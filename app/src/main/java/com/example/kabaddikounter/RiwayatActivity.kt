package com.example.kabaddikounter

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.ui.MatchAdapter
import com.example.kabaddikounter.viewModels.ScoreViewModel
import kotlinx.coroutines.launch

class RiwayatActivity : AppCompatActivity() {

    private val viewModel: ScoreViewModel by viewModels()
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Match History"
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = MatchAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observe data dari ViewModel
        lifecycleScope.launch {
            viewModel.allMatches.collect { matches ->
                adapter.submitList(matches)
            }
        }
    }
}