package com.example.kabaddikounter

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.kabaddikounter.databinding.FragmentHomeBinding
import com.example.kabaddikounter.helper.LiveScoreNotificationHelper
import com.example.kabaddikounter.viewModels.ScoreViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

/**
 * HomeFragment menggantikan logika counter yang sebelumnya ada di MainActivity.
 *
 * Poin penting MVVM:
 * - activityViewModels() → ViewModel di-share dengan Activity dan Fragment lain.
 *   Ini memungkinkan RiwayatFragment juga bisa observe data yang sama.
 * - DataBinding tetap bekerja persis seperti sebelumnya (binding.viewModel = viewModel).
 * - Fragment tidak perlu tahu tentang navigasi ke Riwayat/Settings —
 *   itu urusan DrawerLayout yang sudah di-handle NavController di MainActivity.
 */
class HomeFragment : Fragment() {

    // activityViewModels() = ViewModel hidup selama Activity hidup,
    // bisa di-share antar Fragment dalam satu Activity
    private val viewModel: ScoreViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // DataBinding setup — sama persis dengan sebelumnya
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Simpan Match
        binding.buttonSimpan.setOnClickListener {
            showSaveDialog()
        }

        // Observer status simpan
        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveStatus()
            }
        }

        // Export JSON
        binding.buttonExport.setOnClickListener {
            exportMatchToJson()
        }

        // Setup notifikasi live score
        setupLiveScoreNotification()

//        // Aktifkan hamburger icon di Fragment
//        val navController = findNavController()
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.homeFragment, R.id.riwayatFragment, R.id.settingsFragment),
//            (requireActivity() as MainActivity).findViewById(R.id.drawerLayout)
//        )
//        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
//        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setupLiveScoreNotification() {
        LiveScoreNotificationHelper.createChannel(requireContext())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        viewModel.scoreA.observe(viewLifecycleOwner) {
            LiveScoreNotificationHelper.showLiveScore(
                context = requireContext(),
                teamA = viewModel.teamA.value ?: "",
                teamB = viewModel.teamB.value ?: "",
                scoreA = viewModel.scoreA.value ?: 0,
                scoreB = viewModel.scoreB.value ?: 0
            )
        }

        viewModel.scoreB.observe(viewLifecycleOwner) {
            LiveScoreNotificationHelper.showLiveScore(
                context = requireContext(),
                teamA = viewModel.teamA.value ?: "",
                teamB = viewModel.teamB.value ?: "",
                scoreA = viewModel.scoreA.value ?: 0,
                scoreB = viewModel.scoreB.value ?: 0
            )
        }
    }

    private fun showSaveDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Masukkan judul match"
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Simpan Match")
            .setMessage("Masukkan judul untuk menyimpan hasil pertandingan ini.")
            .setView(editText)
            .setPositiveButton("Simpan") { dialog, _ ->
                val title = editText.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.saveMatch(title)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun exportMatchToJson() {
        viewLifecycleOwner.lifecycleScope.launch {
            val matches = viewModel.getAllMatches()

            if (matches.isEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada data match!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(matches)

            val fileName = "matches_export_${System.currentTimeMillis()}.json"
            val file = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )
            FileWriter(file).use { it.write(jsonString) }

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Export JSON via..."))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LiveScoreNotificationHelper.cancelNotification(requireContext())
        _binding = null // Wajib untuk mencegah memory leak di Fragment
    }
}