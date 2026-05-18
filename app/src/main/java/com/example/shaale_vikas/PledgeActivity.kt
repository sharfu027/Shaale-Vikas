package com.example.shaale_vikas

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shaale_vikas.databinding.ActivityPledgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PledgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPledgeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var needId: String? = null
    private var needTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPledgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        needId = intent.getStringExtra("NEED_ID")
        needTitle = intent.getStringExtra("NEED_TITLE")

        binding.tvPledgeTarget.text = getString(R.string.for_project, needTitle ?: "")
        
        // Auto-fill donor name from profile
        val currentUser = auth.currentUser
        binding.etPledgeName.setText(currentUser?.displayName ?: "")

        binding.btnSubmitPledge.setOnClickListener {
            submitPledge()
        }
    }

    private fun submitPledge() {
        val donorName = binding.etPledgeName.text.toString().trim()
        val amountStr = binding.etPledgeAmount.text.toString().trim()
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        if (donorName.isEmpty() || amount <= 0) {
            Toast.makeText(this, "Please enter a valid name and amount", Toast.LENGTH_SHORT).show()
            return
        }

        val pledge = Pledge(
            needId = needId ?: "",
            needTitle = needTitle ?: "",
            donorId = auth.currentUser?.uid ?: "",
            donorName = donorName,
            amount = amount,
            status = "PENDING"
        )

        db.collection("pledges").add(pledge)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you! Your pledge is pending approval.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
