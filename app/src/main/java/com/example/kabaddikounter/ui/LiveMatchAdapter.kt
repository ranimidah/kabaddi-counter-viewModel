package com.example.kabaddikounter.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.databinding.ItemLiveMatchBinding
import com.example.kabaddikounter.service.MatchApiData

class LiveMatchAdapter(
    private val onSubscribe: (MatchApiData) -> Unit,
    private val onMatchClick: (MatchApiData) -> Unit
) : ListAdapter<MatchApiData, LiveMatchAdapter.ViewHolder>(DiffCallback()) {

    private var subscribedMatchId: Int? = null
    fun setSubscribedMatchId(id: Int?) {
        subscribedMatchId = id
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemLiveMatchBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: MatchApiData) {
            binding.match = match

            binding.isSubscribed = match.id == subscribedMatchId
            binding.btnSubscribe.setOnClickListener {
                onSubscribe(match)
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

    class DiffCallback : DiffUtil.ItemCallback<MatchApiData>() {
        override fun areItemsTheSame(oldItem: MatchApiData, newItem: MatchApiData) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MatchApiData, newItem: MatchApiData) =
            oldItem == newItem
    }
}