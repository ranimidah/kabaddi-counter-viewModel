package com.example.kabaddikounter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.viewModels.LiveScoreViewModel

class MatchListActivity : AppCompatActivity() {

    private val viewModel: LiveScoreViewModel by viewModels()
    private lateinit var adapter: LiveMatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        adapter = LiveMatchAdapter { match ->
            viewModel.subscribeToMatch(match)
            Toast.makeText(this, "Berlangganan ke ${match.teamA} vs ${match.teamB}", Toast.LENGTH_SHORT).show()
            finish() // kembali ke MainActivity
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.matches.observe(this, Observer { matches ->
            adapter.submitList(matches)
        })

        viewModel.error.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loadMatches()
    }
}