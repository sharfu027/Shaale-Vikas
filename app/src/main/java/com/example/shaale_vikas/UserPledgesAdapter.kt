package com.example.shaale_vikas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shaale_vikas.databinding.ItemUserPledgeBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UserPledgesAdapter : ListAdapter<Pledge, UserPledgesAdapter.PledgeViewHolder>(PledgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PledgeViewHolder {
        val binding = ItemUserPledgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PledgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PledgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PledgeViewHolder(private val binding: ItemUserPledgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pledge: Pledge) {
            binding.tvUserPledgeTitle.text = pledge.needTitle
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvUserPledgeAmount.text = currencyFormat.format(pledge.amount)
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvUserPledgeDate.text = pledge.timestamp?.let { sdf.format(it) } ?: "Pending"
            
            binding.tvUserPledgeStatus.text = pledge.status
            
            // Set background color based on status
            val backgroundRes = when (pledge.status) {
                "APPROVED" -> R.drawable.bg_status_approved
                "REJECTED" -> R.drawable.bg_status_rejected
                else -> R.drawable.bg_status_pending
            }
            binding.tvUserPledgeStatus.setBackgroundResource(backgroundRes)
        }
    }

    class PledgeDiffCallback : DiffUtil.ItemCallback<Pledge>() {
        override fun areItemsTheSame(oldItem: Pledge, newItem: Pledge): Boolean =
            oldItem.firebaseId == newItem.firebaseId

        override fun areContentsTheSame(oldItem: Pledge, newItem: Pledge): Boolean =
            oldItem == newItem
    }
}
