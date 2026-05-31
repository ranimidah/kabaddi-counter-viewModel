package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.FragmentRiwayatBinding
import com.example.kabaddikounter.ui.RiwayatAdapter
import com.example.kabaddikounter.viewModels.ScoreViewModel
import kotlinx.coroutines.launch

class RiwayatFragment : Fragment() {

    companion object {
        fun newInstance() = RiwayatFragment()
    }

    private val viewModel: ScoreViewModel by activityViewModels()

    // Binding di-nullable supaya bisa di-clear di onDestroyView (best practice Fragment)
    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RiwayatAdapter

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
        viewModel.fetchMatchHistory()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeMatches() {
        // Data dari API
        viewModel.matchHistory.observe(viewLifecycleOwner) { matches ->
            adapter.submitList(matches)
        }

        // Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Error
        viewModel.fetchError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Wajib di-null-kan untuk menghindari memory leak di Fragment
        _binding = null
    }
}