package com.example.shaale_vikas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shaale_vikas.databinding.ItemRecentPledgeBinding
import java.text.NumberFormat
import java.util.Locale
import android.text.format.DateUtils

class RecentPledgesAdapter : ListAdapter<Pledge, RecentPledgesAdapter.RecentPledgeViewHolder>(RecentPledgeDiffCallback()) {

    inner class RecentPledgeViewHolder(private val binding: ItemRecentPledgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pledge: Pledge) {
            binding.tvRecentDonorName.text = pledge.donorName
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvRecentAmount.text = currencyFormat.format(pledge.amount)
            
            if (pledge.needTitle.isNotEmpty()) {
                binding.tvRecentProjectName.text = binding.root.context.getString(R.string.for_project, pledge.needTitle)
                binding.tvRecentProjectName.visibility = android.view.View.VISIBLE
            } else {
                binding.tvRecentProjectName.visibility = android.view.View.GONE
            }
            
            val timeAgo = pledge.timestamp?.let {
                DateUtils.getRelativeTimeSpanString(it.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            } ?: "Just now"
            
            binding.tvRecentTime.text = timeAgo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentPledgeViewHolder {
        val binding = ItemRecentPledgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentPledgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentPledgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecentPledgeDiffCallback : DiffUtil.ItemCallback<Pledge>() {
        override fun areItemsTheSame(oldItem: Pledge, newItem: Pledge): Boolean {
            return oldItem.firebaseId == newItem.firebaseId
        }

        override fun areContentsTheSame(oldItem: Pledge, newItem: Pledge): Boolean {
            return oldItem == newItem
        }
    }
}
