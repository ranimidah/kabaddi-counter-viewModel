package com.example.kabaddikounter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.databinding.ItemLastUpdateBinding
import com.example.kabaddikounter.data.MatchUpdate

class LastUpdateAdapter :
    ListAdapter<MatchUpdate, LastUpdateAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemLastUpdateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MatchUpdate) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLastUpdateBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<MatchUpdate>() {
        override fun areItemsTheSame(oldItem: MatchUpdate, newItem: MatchUpdate) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MatchUpdate, newItem: MatchUpdate) =
            oldItem == newItem
    }
}