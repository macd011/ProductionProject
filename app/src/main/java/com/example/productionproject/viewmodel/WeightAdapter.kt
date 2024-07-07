package com.example.productionproject.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productionproject.R

data class WeightEntry(
    val id: String = "",
    val date: String = "",
    var weight: Double = 0.0,
    var unit: String = "kg"
)

class WeightAdapter(
    var weightEntries: MutableList<WeightEntry>,
    private val itemClickListener: (WeightEntry) -> Unit,
    private val deleteClickListener: (WeightEntry) -> Unit
) : RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    inner class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)

        fun bind(weightEntry: WeightEntry) {
            tvDate.text = weightEntry.date
            tvWeight.text = "${weightEntry.weight} ${weightEntry.unit}"
            itemView.setOnClickListener {
                itemClickListener(weightEntry)
            }
            itemView.setOnLongClickListener {
                deleteClickListener(weightEntry)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weight_entry, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(weightEntries[position])
    }

    override fun getItemCount(): Int {
        return weightEntries.size
    }

    fun updateWeightEntries(newEntries: List<WeightEntry>) {
        weightEntries.clear()
        weightEntries.addAll(newEntries.sortedBy { it.date })
        notifyDataSetChanged()
    }
}
