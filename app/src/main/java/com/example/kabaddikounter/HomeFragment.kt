package com.example.kabaddikounter

import android.Manifest
import android.app.AlertDialog
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.databinding.FragmentHomeBinding
import com.example.kabaddikounter.viewModels.LiveScoreViewModel
import com.example.kabaddikounter.viewModels.ScoreViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class HomeFragment : Fragment() {

    private val viewModel: ScoreViewModel by activityViewModels()
    private val liveScoreViewModel: LiveScoreViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Launcher for MatchListActivity; receives the selected match and triggers subscription
    private val matchListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val match: Match? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(MatchListActivity.EXTRA_MATCH, Match::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(MatchListActivity.EXTRA_MATCH)
            }
            match?.let { liveScoreViewModel.subscribeToMatch(it) }
        }
    }

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
            matchListLauncher.launch(Intent(requireActivity(), MatchListActivity::class.java))
        }

        // Reset button: clears subscription (if any) and resets counter to defaults
        binding.buttonReset.setOnClickListener {
            liveScoreViewModel.resetSubscription()
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

        // Observe subscription error
        liveScoreViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe subscribed match → update counter and lock/unlock UI
        liveScoreViewModel.subscribedMatch.observe(viewLifecycleOwner) { match ->
            if (match != null) {
                viewModel.setLiveMatchData(match.teamA, match.teamB, match.scoreA, match.scoreB)
                viewModel.setSubscribed(true)

                val statusText = if (match.status == "FINISHED") {
                    "Pertandingan berakhir: ${match.teamA} vs ${match.teamB}"
                } else {
                    "Berlangganan: ${match.teamA} vs ${match.teamB}"
                }
                binding.tvSubscribeStatus.text = statusText
                binding.subscribeStatusBar.visibility = View.VISIBLE
            } else {
                viewModel.setSubscribed(false)
                binding.subscribeStatusBar.visibility = View.GONE
            }
        }

        // Update live score notification whenever counter changes
        viewModel.scoreA.observe(viewLifecycleOwner) { updateLiveScoreNotification() }
        viewModel.scoreB.observe(viewLifecycleOwner) { updateLiveScoreNotification() }
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
