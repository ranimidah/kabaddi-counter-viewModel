package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.FragmentDetailMatchBinding
import com.example.kabaddikounter.ui.LastUpdateAdapter
import com.example.kabaddikounter.viewModels.DetailMatchViewModel

class DetailMatchFragment : Fragment() {

    companion object {
        private const val ARG_MATCH_ID = "match_id"

        fun newInstance(matchId: String): DetailMatchFragment {
            return DetailMatchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MATCH_ID, matchId)
                }
            }
        }
    }

    // ── DataBinding & ViewModel ───────────────────────────────────────────────

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

        val matchId = arguments?.getString(ARG_MATCH_ID) ?: ""
        viewModel.loadMatch(matchId)
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
}