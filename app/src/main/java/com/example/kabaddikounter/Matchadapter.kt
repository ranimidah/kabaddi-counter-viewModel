package com.example.kabaddikounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kabaddikounter.data.MatchEntity

class MatchAdapter : ListAdapter<MatchEntity, MatchAdapter.MatchViewHolder>(DiffCallback()) {

    // Menyimpan posisi item yang sedang di-expand
    private var expandedPosition: Int = -1

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textWinner: TextView = itemView.findViewById(R.id.textWinner)
        val textScoreA: TextView = itemView.findViewById(R.id.textScoreA)
        val textScoreB: TextView = itemView.findViewById(R.id.textScoreB)
        val layoutHeader: LinearLayout = itemView.findViewById(R.id.layoutHeader)
        val layoutDetail: LinearLayout = itemView.findViewById(R.id.layoutDetail)
        val iconChevron: ImageView = itemView.findViewById(R.id.iconChevron)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = getItem(position)
        val isExpanded = position == expandedPosition

        // Tentukan pemenang
        val winner = when {
            match.scoreA > match.scoreB -> match.teamAName
            match.scoreB > match.scoreA -> match.teamBName
            else -> "Seri"
        }

        holder.textTitle.text = "${match.teamAName} vs ${match.teamBName}"
        holder.textWinner.text = "Pemenang: $winner"
        holder.textScoreA.text = "${match.teamAName} : ${match.scoreA}"
        holder.textScoreB.text = "${match.teamBName} : ${match.scoreB}"

        // Tampilkan atau sembunyikan detail
        holder.layoutDetail.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.iconChevron.rotation = if (isExpanded) 180f else 0f

        // Klik header untuk expand/collapse
        holder.layoutHeader.setOnClickListener {
            val previousExpanded = expandedPosition
            expandedPosition = if (isExpanded) -1 else holder.adapterPosition

            if (previousExpanded != -1) notifyItemChanged(previousExpanded)
            notifyItemChanged(holder.adapterPosition)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MatchEntity>() {
        override fun areItemsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
            oldItem == newItem
    }
}