package com.example.kabaddikounter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.ActivityMatchListBinding
import com.example.kabaddikounter.repository.MatchRepository
import com.example.kabaddikounter.viewModels.MatchListViewModel
import com.example.kabaddikounter.viewModels.MatchListViewModelFactory

class MatchListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchListBinding

    private val viewModel: MatchListViewModel by viewModels {
        MatchListViewModelFactory(MatchRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupRecyclerView()
        observeViewModel()
        viewModel.loadMatches()
    }

    private fun setupRecyclerView() {
        binding.rvMatches.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.matches.observe(this) { matches ->
            // TODO: update adapter
        }
        viewModel.subscribedMatch.observe(this) { match ->
            // TODO: tampilkan live score panel
        }
        viewModel.error.observe(this) { message ->
            message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }
}