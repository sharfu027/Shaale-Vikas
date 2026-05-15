package com.example.shaale_vikas

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shaale_vikas.databinding.ItemNeedBinding
import java.text.NumberFormat
import java.util.Locale

class NeedsAdapter(
    private val onPledgeClick: (Need) -> Unit,
    private val onLongClick: (Need) -> Unit
) : ListAdapter<Need, NeedsAdapter.NeedViewHolder>(NeedDiffCallback()) {

    inner class NeedViewHolder(private val binding: ItemNeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(need: Need) {
            val context = binding.root.context
            binding.tvTitle.text = need.title
            binding.tvDescription.text = need.description
            binding.tvCategory.text = need.category
            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            val currentFormatted = currencyFormat.format(need.currentAmount)
            val estimatedFormatted = currencyFormat.format(need.estimatedCost)
            
            binding.tvStatus.text = context.getString(R.string.collected_status, currentFormatted, estimatedFormatted)
            
            val progress = need.getProgress()
            binding.progressBar.progress = progress
            binding.tvProgressPercent.text = context.getString(R.string.progress_percent, progress)

            val displayImageUrl = if (need.status == "COMPLETED" && need.afterImageUrl.isNotEmpty()) {
                need.afterImageUrl
            } else {
                need.imageUrl
            }

            Glide.with(binding.ivNeedImage.context)
                .load(displayImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.ivNeedImage)

            when (need.status) {
                "COMPLETED" -> {
                    binding.btnPledge.visibility = View.GONE
                    binding.tvProgressPercent.text = context.getString(R.string.status_completed)
                }
                "PLEDGED" -> {
                    binding.btnPledge.visibility = View.VISIBLE
                    binding.btnPledge.text = context.getString(R.string.status_pledged)
                    binding.btnPledge.isEnabled = false
                }
                else -> {
                    binding.btnPledge.visibility = View.VISIBLE
                    binding.btnPledge.text = context.getString(R.string.pledge_support)
                    binding.btnPledge.isEnabled = true
                }
            }

            binding.btnPledge.setOnClickListener { onPledgeClick(need) }
            
            binding.btnShare.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject, need.title))
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, need.title, estimatedFormatted))
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
            }

            binding.root.setOnLongClickListener {
                onLongClick(need)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val binding = ItemNeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NeedDiffCallback : DiffUtil.ItemCallback<Need>() {
        override fun areItemsTheSame(oldItem: Need, newItem: Need): Boolean {
            return oldItem.firebaseId == newItem.firebaseId
        }

        override fun areContentsTheSame(oldItem: Need, newItem: Need): Boolean {
            return oldItem == newItem
        }
    }
}
