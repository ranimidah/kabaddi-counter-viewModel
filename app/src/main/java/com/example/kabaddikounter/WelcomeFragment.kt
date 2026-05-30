package com.example.kabaddikounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kabaddikounter.databinding.FragmentWelcomeBinding
import com.example.kabaddikounter.viewModels.WelcomeViewModel

/**
 * WelcomeFragment — halaman pemilihan role.
 *
 * Pola MVVM yang diikuti (sama dengan HomeFragment):
 * - DataBinding: binding.viewModel = viewModel  →  onClick langsung di XML
 * - viewModels() bukan activityViewModels() karena WelcomeViewModel tidak perlu di-share
 * - Navigasi via NavController (findNavController().navigate(...))
 * - Fragment hanya observe event dari ViewModel, tidak ada logic bisnis di sini
 *
 * Flow navigasi:
 *   ADMIN  → action_welcomeFragment_to_homeFragment
 *   VIEWER → action_welcomeFragment_to_matchListFragment
 *   (kedua action pakai popUpTo welcomeFragment inclusive=true
 *    sehingga WelcomeFragment dikeluarkan dari back stack)
 */
class WelcomeFragment : Fragment() {

    // viewModels() — scope Fragment saja, tidak perlu di-share ke Fragment lain
    private val viewModel: WelcomeViewModel by viewModels()

    // Nullable binding, wajib null-kan di onDestroyView (sama dengan HomeFragment)
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // DataBinding inflate — sama persis polanya dengan HomeFragment
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNavigation()
    }

    /**
     * Observe event navigasi dari ViewModel.
     * Navigasi dilakukan di Fragment karena NavController adalah UI concern.
     */
    private fun observeNavigation() {
        viewModel.navigateTo.observe(viewLifecycleOwner) { role ->
            role ?: return@observe  // null = event sudah dikonsumsi, abaikan

            val action = when (role) {
                WelcomeViewModel.Role.ADMIN ->
                    WelcomeFragmentDirections.actionWelcomeFragmentToHomeFragment()

                WelcomeViewModel.Role.VIEWER ->
                    WelcomeFragmentDirections.actionWelcomeFragmentToMatchListFragment()
            }

            findNavController().navigate(action)

            // Tandai event sudah dikonsumsi agar tidak re-navigate saat rotation
            viewModel.onNavigationHandled()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Wajib — mencegah memory leak (sama dengan HomeFragment)
    }
}