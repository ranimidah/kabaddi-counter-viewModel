package com.example.kabaddikounter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.databinding.ItemLiveMatchBinding

class LiveMatchAdapter(
    private val onSubscribe: (Match) -> Unit,
    private val onMatchClick: (Match) -> Unit
) : ListAdapter<Match, LiveMatchAdapter.ViewHolder>(DiffCallback()) {

    private var subscribedMatchId: String? = null
    fun setSubscribedMatchId(id: String?) {
        subscribedMatchId = id
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemLiveMatchBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.match = match

            binding.isSubscribed = match.id.isNotBlank() && match.id == subscribedMatchId
            binding.btnSubscribe.setOnClickListener {
                if (match.id.isNotBlank()) onSubscribe(match)
            }

            // binding.isSubscribed = isSubscribed
            binding.root.setOnClickListener {
                onMatchClick(match)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLiveMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = getItem(position) ?: return
        holder.bind(match)
    }

    class DiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Match, newItem: Match) =
            oldItem == newItem
    }
}