package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shaale_vikas.databinding.ActivitySuccessStoriesBinding
import com.google.firebase.firestore.FirebaseFirestore

class SuccessStoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessStoriesBinding
    private lateinit var adapter: NeedsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        fetchSuccessStories()
    }

    private fun setupRecyclerView() {
        adapter = NeedsAdapter(
            onPledgeClick = { /* No pledges on success stories */ },
            onLongClick = { /* No admin options here */ }
        )
        binding.rvSuccessStories.layoutManager = LinearLayoutManager(this)
        binding.rvSuccessStories.adapter = adapter
    }

    private fun fetchSuccessStories() {
        db.collection("needs")
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener { documents ->
                val stories = documents.toObjects(Need::class.java)
                if (stories.isEmpty()) {
                    binding.tvEmptySuccess.visibility = View.VISIBLE
                    binding.rvSuccessStories.visibility = View.GONE
                } else {
                    binding.tvEmptySuccess.visibility = View.GONE
                    binding.rvSuccessStories.visibility = View.VISIBLE
                    adapter.submitList(stories)
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
