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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kabaddikounter.databinding.FragmentHomeBinding
import com.example.kabaddikounter.helper.LiveScoreNotificationHelper
import com.example.kabaddikounter.viewModels.ScoreViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class HomeFragment : Fragment() {

    private val viewModel: ScoreViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNotificationPermission()
        LiveScoreNotificationHelper.createChannel(requireContext())

        // Subscribe button → open match list
        binding.btnOpenMatchList.setOnClickListener {
            startActivity(Intent(requireActivity(), MatchListActivity::class.java))
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            viewModel.setSubscribed(false)
            viewModel.reset()
        }

        // Simpan match
        binding.buttonSimpan.setOnClickListener {
            showSaveDialog()
        }

        // Export JSON
        binding.buttonExport.setOnClickListener {
            exportMatchToJson()
        }

        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveStatus()
            }
        }

        // Update live score notification whenever counter changes
        viewModel.scoreA.observe(viewLifecycleOwner) { updateLiveScoreNotification() }
        viewModel.scoreB.observe(viewLifecycleOwner) { updateLiveScoreNotification() }

        binding.buttonEndLive.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Akhiri Match")
                .setMessage("Yakin ingin mengakhiri pertandingan ini?")
                .setPositiveButton("Ya") { _, _ -> viewModel.endMatch() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun updateLiveScoreNotification() {
        LiveScoreNotificationHelper.showLiveScore(
            context = requireContext(),
            teamA = viewModel.teamA.value ?: "",
            teamB = viewModel.teamB.value ?: "",
            scoreA = viewModel.scoreA.value ?: 0,
            scoreB = viewModel.scoreB.value ?: 0
        )
    }

    private fun setupNotificationPermission() {
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

        viewModel.currentMatch.observe(viewLifecycleOwner) { match ->
            if (match != null) {
                binding.teamAName.setText(match.team_a)
                binding.teamBName.setText(match.team_b)
            }
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
        _binding = null
    }
}
