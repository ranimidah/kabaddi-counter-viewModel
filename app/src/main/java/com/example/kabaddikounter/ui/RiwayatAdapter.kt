package com.example.kabaddikounter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.MatchEntity
import com.example.kabaddikounter.databinding.ItemRiwayatBinding
import com.example.kabaddikounter.service.MatchApiData

class RiwayatAdapter : ListAdapter<MatchApiData, RiwayatAdapter.RiwayatViewHolder>(DiffCallback()) {

    private var expandedPosition: Int = -1

    inner class RiwayatViewHolder(
        private val binding: ItemRiwayatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: MatchApiData, isExpanded: Boolean) {
            binding.match = match
            binding.isExpanded = isExpanded
            binding.executePendingBindings()

            binding.layoutHeader.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else adapterPosition

                if (previousExpanded != -1) notifyItemChanged(previousExpanded)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val binding = ItemRiwayatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RiwayatViewHolder(binding)
    }

    // Hapus argumen kedua (isExpanded)
    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    // Ganti MatchEntity → MatchApiData di sini juga
    class DiffCallback : DiffUtil.ItemCallback<MatchApiData>() {
        override fun areItemsTheSame(oldItem: MatchApiData, newItem: MatchApiData) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MatchApiData, newItem: MatchApiData) =
            oldItem == newItem
    }
}