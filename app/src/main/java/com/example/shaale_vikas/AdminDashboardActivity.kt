package com.example.shaale_vikas

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shaale_vikas.databinding.ActivityAdminDashboardBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdminPledgesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        listenToPledges()
        fetchAnalytics()
    }

    private fun setupRecyclerView() {
        adapter = AdminPledgesAdapter(
            onApproveClick = { pledge -> approvePledge(pledge) },
            onRejectClick = { pledge -> rejectPledge(pledge) }
        )
        binding.rvPendingPledges.layoutManager = LinearLayoutManager(this)
        binding.rvPendingPledges.adapter = adapter
    }

    private fun listenToPledges() {
        db.collection("pledges")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val pledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                adapter.submitList(pledges)
            }
    }

    private fun fetchAnalytics() {
        // Total Funding (Approved Pledges)
        db.collection("pledges")
            .whereEqualTo("status", "APPROVED")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val approvedPledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                val totalAmount = approvedPledges.sumOf { it.amount }
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                binding.tvAdminTotalFunding.text = currencyFormat.format(totalAmount)
            }

        // Completed Projects
        db.collection("needs")
            .whereEqualTo("status", "COMPLETED")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val count = value?.size() ?: 0
                binding.tvAdminCompletedProjects.text = count.toString()
            }
    }

    private fun approvePledge(pledge: Pledge) {
        val batch = db.batch()
        
        val pledgeRef = db.collection("pledges").document(pledge.firebaseId)
        batch.update(pledgeRef, "status", "APPROVED")
        
        val needRef = db.collection("needs").document(pledge.needId)
        batch.update(needRef, "currentAmount", FieldValue.increment(pledge.amount))
        
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.pledge_approved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to approve: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectPledge(pledge: Pledge) {
        db.collection("pledges").document(pledge.firebaseId)
            .update("status", "REJECTED")
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.pledge_rejected), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to reject: ${e.message}", Toast.LENGTH_SHORT).show()
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
