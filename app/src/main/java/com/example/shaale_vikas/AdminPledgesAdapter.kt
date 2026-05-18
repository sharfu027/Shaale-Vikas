package com.example.shaale_vikas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shaale_vikas.databinding.ItemPendingPledgeBinding
import java.text.NumberFormat
import java.util.Locale

class AdminPledgesAdapter(
    private val onApproveClick: (Pledge) -> Unit,
    private val onRejectClick: (Pledge) -> Unit
) : ListAdapter<Pledge, AdminPledgesAdapter.PledgeViewHolder>(PledgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PledgeViewHolder {
        val binding = ItemPendingPledgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PledgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PledgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PledgeViewHolder(private val binding: ItemPendingPledgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pledge: Pledge) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvDonorName.text = pledge.donorName
            binding.tvPledgeInfo.text = "${currencyFormat.format(pledge.amount)} for ${pledge.needTitle}"
            
            binding.btnApprove.setOnClickListener {
                onApproveClick(pledge)
            }
            
            binding.btnReject.setOnClickListener {
                onRejectClick(pledge)
            }
        }
    }

    class PledgeDiffCallback : DiffUtil.ItemCallback<Pledge>() {
        override fun areItemsTheSame(oldItem: Pledge, newItem: Pledge): Boolean =
            oldItem.firebaseId == newItem.firebaseId

        override fun areContentsTheSame(oldItem: Pledge, newItem: Pledge): Boolean =
            oldItem == newItem
    }
}
