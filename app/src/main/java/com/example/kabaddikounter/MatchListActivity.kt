package com.example.kabaddikounter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabaddikounter.databinding.ActivityMatchListBinding
import com.example.kabaddikounter.repository.MatchRepository
import com.example.kabaddikounter.service.MatchApiData
import com.example.kabaddikounter.ui.LiveMatchAdapter
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

        viewModel.subscribedMatch.observe(this) { match: MatchApiData? ->
            (binding.rvMatches.adapter as? LiveMatchAdapter)
                ?.setSubscribedMatchId(match?.id)  // ← tidak perlu toString() lagi
        }
    }

    private fun setupRecyclerView() {
        binding.rvMatches.layoutManager = LinearLayoutManager(this)

        val adapter = LiveMatchAdapter(
            onSubscribe = { match ->
                val current = viewModel.subscribedMatch.value
                if (current?.id == match.id) {
                    viewModel.unsubscribeFromMatch(match) // toggle off
                } else {
                    viewModel.subscribeToMatch(match)     // toggle on
                }
            },
            onMatchClick = { match ->
                // navigasi ke detail jika perlu
            }
        )
        binding.rvMatches.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.matches.observe(this) { matches ->
            // TODO: update adapter
            (binding.rvMatches.adapter as? LiveMatchAdapter)?.submitList(matches)
        }
        viewModel.subscribedMatch.observe(this) { match ->
            // TODO: tampilkan live score panel
            (binding.rvMatches.adapter as? LiveMatchAdapter)
                ?.setSubscribedMatchId(match?.id)
        }
        viewModel.error.observe(this) { message ->
            message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }
}