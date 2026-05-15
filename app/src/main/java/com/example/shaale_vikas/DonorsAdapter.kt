package com.example.shaale_vikas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shaale_vikas.databinding.ItemDonorBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DonorsAdapter : ListAdapter<Pledge, DonorsAdapter.DonorViewHolder>(PledgeDiffCallback()) {

    inner class DonorViewHolder(private val binding: ItemDonorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(donor: Pledge) {
            binding.tvDonorName.text = donor.donorName
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvAmount.text = currencyFormat.format(donor.amount)
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = donor.timestamp?.let { sdf.format(it) } ?: "Recently"
            binding.tvDate.text = binding.root.context.getString(R.string.pledged_on, dateStr)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PledgeDiffCallback : DiffUtil.ItemCallback<Pledge>() {
        override fun areItemsTheSame(oldItem: Pledge, newItem: Pledge): Boolean {
            return oldItem.firebaseId == newItem.firebaseId
        }

        override fun areContentsTheSame(oldItem: Pledge, newItem: Pledge): Boolean {
            return oldItem == newItem
        }
    }
}
