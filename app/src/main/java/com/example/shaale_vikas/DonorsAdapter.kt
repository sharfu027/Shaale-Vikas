package com.example.shaale_vikas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shaale_vikas.databinding.ItemDonorBinding
import java.text.NumberFormat
import java.util.Locale

class DonorsAdapter : ListAdapter<DonorStats, DonorsAdapter.DonorViewHolder>(DonorStatsDiffCallback()) {

    inner class DonorViewHolder(private val binding: ItemDonorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stats: DonorStats, rank: Int) {
            val context = binding.root.context
            binding.tvDonorName.text = context.getString(R.string.donor_rank_name, rank, stats.donorName)
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvAmount.text = currencyFormat.format(stats.totalAmount)
            
            binding.tvDate.text = if (stats.pledgeCount == 1) {
                context.getString(R.string.one_contribution)
            } else {
                context.getString(R.string.multiple_contributions, stats.pledgeCount)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class DonorStatsDiffCallback : DiffUtil.ItemCallback<DonorStats>() {
        override fun areItemsTheSame(oldItem: DonorStats, newItem: DonorStats): Boolean {
            return oldItem.donorId == newItem.donorId
        }

        override fun areContentsTheSame(oldItem: DonorStats, newItem: DonorStats): Boolean {
            return oldItem == newItem
        }
    }
}
