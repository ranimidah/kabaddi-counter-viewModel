package com.example.kabaddikounter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.viewModels.LiveScoreViewModel

class MatchListActivity : AppCompatActivity() {

    private val viewModel: LiveScoreViewModel by viewModels()
    private lateinit var adapter: LiveMatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        adapter = LiveMatchAdapter { match ->
            // Return the selected match to caller (HomeFragment via ActivityResultLauncher)
            val resultIntent = Intent().apply {
                putExtra(EXTRA_MATCH, match)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.matches.observe(this) { matches ->
            adapter.submitList(matches)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadMatches()
    }

    companion object {
        const val EXTRA_MATCH = "extra_match"
    }
}
