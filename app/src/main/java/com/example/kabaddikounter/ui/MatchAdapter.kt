// ui/MatchAdapter.kt
package com.example.kabaddikounter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.MatchEntity
import com.example.kabaddikounter.databinding.ItemMatchBinding

class MatchAdapter : ListAdapter<MatchEntity, MatchAdapter.MatchViewHolder>(DiffCallback()) {

    private var expandedPosition: Int = -1

    inner class MatchViewHolder(
        private val binding: ItemMatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: MatchEntity, isExpanded: Boolean) {
            binding.match = match
            binding.isExpanded = isExpanded
            binding.executePendingBindings() // Pastikan binding langsung dieksekusi

            binding.layoutHeader.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else adapterPosition

                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    class DiffCallback : DiffUtil.ItemCallback<MatchEntity>() {
        override fun areItemsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
            oldItem == newItem
    }
}