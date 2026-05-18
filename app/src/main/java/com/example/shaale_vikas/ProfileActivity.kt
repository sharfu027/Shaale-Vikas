package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shaale_vikas.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var adapter: UserPledgesAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        loadUserData()
        loadUserImpact()
        loadPledgeHistory()

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupRecyclerView() {
        adapter = UserPledgesAdapter()
        binding.rvUserPledges.layoutManager = LinearLayoutManager(this)
        binding.rvUserPledges.adapter = adapter
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    binding.tvUserName.text = name
                    binding.tvUserEmail.text = document.getString("email") ?: auth.currentUser?.email
                }
            }
    }

    private fun loadUserImpact() {
        val uid = auth.currentUser?.uid ?: return
        
        // Query pledges by donorId for accuracy
        db.collection("pledges")
            .whereEqualTo("donorId", uid)
            .addSnapshotListener { documents, _ ->
                if (documents != null) {
                    val pledges = documents.toObjects(Pledge::class.java)
                    val approvedPledges = pledges.filter { it.status == "APPROVED" }
                    val totalAmount = approvedPledges.sumOf { it.amount }
                    val count = pledges.size

                    binding.tvUserPledgeCount.text = count.toString()
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    binding.tvUserTotalPledged.text = currencyFormat.format(totalAmount)
                }
            }
    }

    private fun loadPledgeHistory() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("pledges")
            .whereEqualTo("donorId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val pledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                adapter.submitList(pledges)
            }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
