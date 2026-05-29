package com.example.kabaddikounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.Match

class LiveMatchAdapter (
    private val onSubscribe: (Match) -> Unit
) :  ListAdapter<Match, LiveMatchAdapter.ViewHolder>(DiffCallback()){
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textTeams: TextView = view.findViewById(R.id.textTeams)
        val textScore: TextView = view.findViewById(R.id.textScore)
        val textStatus: TextView = view.findViewById(R.id.textStatus)
        val btnSubscribe: Button = view.findViewById(R.id.btnSubscribe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = getItem(position)

        holder.textTeams.text = "${match.teamA} vs ${match.teamB}"
        holder.textScore.text = "${match.scoreA} - ${match.scoreB}"
        holder.textStatus.text = match.status

//        Tombol yg muncul kalo live aja
        holder.btnSubscribe.visibility = if (match.status == "LIVE") View.VISIBLE else View.GONE
        holder.btnSubscribe.setOnClickListener {
            onSubscribe(match)
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Match, newItem: Match) =
            oldItem == newItem
    }
}