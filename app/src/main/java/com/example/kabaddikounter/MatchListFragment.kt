package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.databinding.FragmentMatchListBinding
import com.example.kabaddikounter.repository.MatchRepository
import com.example.kabaddikounter.ui.LiveMatchAdapter
import com.example.kabaddikounter.viewModels.MatchListViewModel
import com.example.kabaddikounter.viewModels.MatchListViewModelFactory
import com.google.android.material.snackbar.Snackbar

class MatchListFragment : Fragment() {

    private var _binding: FragmentMatchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchListViewModel by viewModels {
        MatchListViewModelFactory(MatchRepository(requireContext()))
    }

    private lateinit var adapter: LiveMatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        viewModel.loadMatches()
    }

    private fun setupAdapter() {
        adapter = LiveMatchAdapter (
            onSubscribe = { match ->
                if (match.id.isBlank()) {
                    showError("ID pertandingan tidak valid")
                    return@LiveMatchAdapter
                }
                viewModel.subscribeToMatch(match)
            },
            onMatchClick = { match ->
                val action = MatchListFragmentDirections
                    .actionMatchListToDetailMatch(matchId = match.id)
                findNavController().navigate(action)
            }
        )
        binding.rvMatches.adapter = adapter
    }

    private fun observeViewModel() {
        // Daftar pertandingan
        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            val list = matches.orEmpty()          // aman dari null
            binding.progressBar.isVisible = false
            binding.tvEmpty.isVisible = list.isEmpty()
            binding.rvMatches.isVisible = list.isNotEmpty()
            adapter.submitList(list)
        }

        // Hasil subscribe berhasil
        viewModel.subscribedMatch.observe(viewLifecycleOwner) { match ->
            match ?: return@observe                // abaikan kalau null (setelah reset)
            adapter.setSubscribedMatchId(match.id)
            Snackbar.make(
                binding.root,
                "Subscribe ke ${match.teamA} vs ${match.teamB} berhasil",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Error
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg ?: return@observe             // abaikan null
            binding.progressBar.isVisible = false
            showError(errorMsg)
        }
    }

    private fun navigateToDetail(match: Match) {
        // Jika pakai Navigation Component:
        val action = MatchListFragmentDirections
            .actionMatchListToDetailMatch(matchId = match.id)
        findNavController().navigate(action)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null                            // cegah memory leak
    }
}