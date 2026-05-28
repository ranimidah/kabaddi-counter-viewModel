package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.FragmentRiwayatBinding
import com.example.kabaddikounter.viewModels.ScoreViewModel
import kotlinx.coroutines.launch

class RiwayatFragment : Fragment() {

    companion object {
        fun newInstance() = RiwayatFragment()
    }

    // ViewModel yang sama dengan RiwayatActivity (ScoreViewModel)
    private val viewModel: ScoreViewModel by viewModels()

    // Binding di-nullable supaya bisa di-clear di onDestroyView (best practice Fragment)
    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate dengan DataBinding
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        // Ikat ViewModel ke layout supaya bisa dipakai di XML jika diperlukan
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeMatches()
    }

    private fun setupRecyclerView() {
        adapter = MatchAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeMatches() {
        // Gunakan viewLifecycleOwner.lifecycleScope agar coroutine aman di Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allMatches.collect { matches ->
                adapter.submitList(matches)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Wajib di-null-kan untuk menghindari memory leak di Fragment
        _binding = null
    }
}