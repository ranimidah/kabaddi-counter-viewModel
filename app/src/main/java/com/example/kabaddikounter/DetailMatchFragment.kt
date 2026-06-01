package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchUpdate
import com.example.kabaddikounter.databinding.FragmentDetailMatchBinding
import com.example.kabaddikounter.service.MatchResponse
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.ui.LastUpdateAdapter
import com.example.kabaddikounter.viewModels.DetailMatchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DetailMatchFragment : Fragment() {
    private var _binding: FragmentDetailMatchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailMatchViewModel by viewModels()

    // ── Adapter ───────────────────────────────────────────────────────────────

    private val lastUpdateAdapter = LastUpdateAdapter()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMatchBinding.inflate(inflater, container, false)
        // Bind ViewModel so XML DataBinding expressions resolve
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val args = DetailMatchFragmentArgs.fromBundle(requireArguments())
        val matchId = args.matchId  // Int, dari nav_graph argument
        if (matchId != -1) {
            viewModel.loadMatch(matchId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.rvLastUpdate.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lastUpdateAdapter
        }
    }

//    private fun setupToolbar() {
//        binding.toolbar.setNavigationOnClickListener {
//            // Handle navigation drawer or back press
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        // Update RecyclerView when new updates arrive
        viewModel.lastUpdates.observe(viewLifecycleOwner) { updates ->
            lastUpdateAdapter.submitList(updates)
        }

        // Show error if any
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun getMatchDetail(matchId: Int): Flow<Match> = flow {
        val response = RetrofitClient.apiService.getMatchDetail(matchId)
        if (response.isSuccessful) {
            val detail = response.body()?.data ?: throw Exception("Data kosong")
            emit(Match(
                id         = detail.id,
                team_a     = detail.team_a,
                team_b     = detail.team_b,
                score_a    = detail.score_a,
                score_b    = detail.score_b,
                status     = detail.status,
                match_time = detail.match_time
            ))
        } else {
            throw Exception("Gagal memuat detail (${response.code()})")
        }
    }

    suspend fun getLastUpdates(matchId: Int): Flow<List<MatchUpdate>> = flow {
        val response = RetrofitClient.apiService.getMatchDetail(matchId)
        if (response.isSuccessful) {
            val updates = response.body()?.data?.last_updates ?: emptyList()
            emit(updates.map { log ->
                MatchUpdate(
                    team    = log.team,
                    points  = log.points,
                    score_a = log.score_a,
                    score_b = log.score_b,
                    time    = log.time
                )
            })
        } else {
            throw Exception("Gagal memuat log (${response.code()})")
        }
    }

}