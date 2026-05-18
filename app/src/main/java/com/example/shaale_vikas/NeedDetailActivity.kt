package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.shaale_vikas.databinding.ActivityNeedDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.util.Locale

class NeedDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNeedDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var supportersAdapter: RecentPledgesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNeedDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.detailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        setupSupportersRecyclerView()

        val needId = intent.getStringExtra("NEED_ID") ?: return
        fetchNeedDetails(needId)
        fetchSupporters(needId)
    }

    private fun setupSupportersRecyclerView() {
        supportersAdapter = RecentPledgesAdapter()
        binding.rvSupporters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSupporters.adapter = supportersAdapter
    }

    private fun fetchNeedDetails(needId: String) {
        db.collection("needs").document(needId).get()
            .addOnSuccessListener { document ->
                val need = document.toObject(Need::class.java) ?: return@addOnSuccessListener
                bindNeedData(need)
            }
    }

    private fun fetchSupporters(needId: String) {
        db.collection("pledges")
            .whereEqualTo("needId", needId)
            .whereEqualTo("status", "APPROVED")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val pledges = documents.toObjects(Pledge::class.java)
                if (pledges.isNotEmpty()) {
                    binding.tvSupportersHeader.visibility = View.VISIBLE
                    binding.rvSupporters.visibility = View.VISIBLE
                    supportersAdapter.submitList(pledges)
                } else {
                    binding.tvSupportersHeader.visibility = View.GONE
                    binding.rvSupporters.visibility = View.GONE
                }
            }
    }

    private fun bindNeedData(need: Need) {
        binding.tvDetailTitle.text = need.title
        binding.tvDetailDescription.text = need.description
        binding.tvDetailCategory.text = need.category

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val current = currencyFormat.format(need.currentAmount)
        val estimated = currencyFormat.format(need.estimatedCost)
        binding.tvDetailProgressText.text = getString(R.string.collected_status, current, estimated)

        val progress = need.getProgress()
        binding.detailProgressBar.progress = progress

        Glide.with(this)
            .load(need.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivNeedDetailImage)

        if (need.status == "COMPLETED") {
            binding.btnDetailPledge.visibility = View.GONE
            binding.gallerySection.visibility = View.VISIBLE
            
            Glide.with(this).load(need.beforeImageUrl).into(binding.ivBefore)
            Glide.with(this).load(need.afterImageUrl).into(binding.ivAfter)
        } else {
            binding.btnDetailPledge.visibility = View.VISIBLE
            binding.btnDetailPledge.setOnClickListener {
                val intent = Intent(this, PledgeActivity::class.java).apply {
                    putExtra("NEED_ID", need.firebaseId)
                    putExtra("NEED_TITLE", need.title)
                }
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
