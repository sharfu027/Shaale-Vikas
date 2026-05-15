package com.example.shaale_vikas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shaale_vikas.databinding.ActivityDonorsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DonorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonorsBinding
    private val db = FirebaseFirestore.getInstance()
    private val adapter = DonorsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDonors)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.hall_of_fame)

        setupRecyclerView()
        fetchDonors()
    }

    private fun setupRecyclerView() {
        binding.rvDonors.layoutManager = LinearLayoutManager(this)
        binding.rvDonors.adapter = adapter
    }

    private fun fetchDonors() {
        binding.progressBar.visibility = View.VISIBLE
        
        db.collection("pledges")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                binding.progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(this, getString(R.string.error_fetching_donors, error.message), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val pledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                
                if (pledges.isEmpty()) {
                    binding.tvEmptyDonors.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyDonors.visibility = View.GONE
                }
                
                adapter.submitList(pledges)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
