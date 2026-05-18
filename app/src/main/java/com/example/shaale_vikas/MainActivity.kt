package com.example.shaale_vikas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shaale_vikas.databinding.ActivityMainBinding
import com.example.shaale_vikas.databinding.DialogPledgeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NeedsAdapter
    private lateinit var recentPledgesAdapter: RecentPledgesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var needsListener: ListenerRegistration? = null
    private var pledgesListener: ListenerRegistration? = null
    
    private var isSeeding = false
    private var pendingNeedForPhoto: Need? = null
    private val pickAfterImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && pendingNeedForPhoto != null) {
            uploadAfterImage(pendingNeedForPhoto!!, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerViews()

        binding.fabAddNeed.setOnClickListener {
            startActivity(Intent(this, AddNeedActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        startListening()
        listenToPledges()
    }

    override fun onStop() {
        super.onStop()
        needsListener?.remove()
        pledgesListener?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hall_of_fame -> {
                startActivity(Intent(this, DonorsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun setupRecyclerViews() {
        adapter = NeedsAdapter(
            onPledgeClick = { need -> showPledgeDialog(need) },
            onLongClick = { need -> showAdminOptions(need) }
        )
        binding.rvNeeds.layoutManager = LinearLayoutManager(this)
        binding.rvNeeds.adapter = adapter

        recentPledgesAdapter = RecentPledgesAdapter()
        binding.rvRecentPledges.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentPledges.adapter = recentPledgesAdapter
    }

    private fun startListening() {
        needsListener?.remove()
        // We use a simpler query first to avoid index requirements while testing
        needsListener = db.collection("needs")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    return@addSnapshotListener
                }

                val needsList = value?.toObjects(Need::class.java) ?: emptyList()
                
                if (needsList.isEmpty() && !isSeeding) {
                    seedDefaultData()
                }
                
                updateVisibility(needsList)
                binding.tvActiveNeeds.text = needsList.count { it.status != "COMPLETED" }.toString()
                
                // Sort manually for now to ensure it works without complex indices
                val sortedList = needsList.sortedByDescending { it.status == "OPEN" }
                adapter.submitList(sortedList)
            }
    }

    private fun updateVisibility(needsList: List<Need>) {
        if (needsList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.rvNeeds.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.rvNeeds.visibility = View.VISIBLE
        }
    }

    private fun handleFirestoreError(error: Exception) {
        Log.e("MainActivity", "Firestore Error: ${error.message}")
        if (error.message?.contains("PERMISSION_DENIED") == true) {
            Toast.makeText(this, "Permission Denied! Please update Firestore Rules in Firebase Console to 'Test Mode'.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error loading data: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seedDefaultData() {
        isSeeding = true
        Log.d("MainActivity", "Seeding default data...")
        
        val need1 = Need(
            title = "New Library Books",
            description = "Providing updated science and literature books for students.",
            category = "Learning Materials",
            estimatedCost = 15000.0,
            currentAmount = 2500.0,
            imageUrl = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=1000"
        )
        val need2 = Need(
            title = "Clean Drinking Water",
            description = "Installation of a new RO water filtration system.",
            category = "Sanitation",
            estimatedCost = 25000.0,
            currentAmount = 500.0,
            imageUrl = "https://images.unsplash.com/photo-1548936236-442d35a09804?q=80&w=1000"
        )

        val batch = db.batch()
        val doc1 = db.collection("needs").document()
        val doc2 = db.collection("needs").document()
        
        batch.set(doc1, need1)
        batch.set(doc2, need2)
        
        batch.commit()
            .addOnSuccessListener { 
                Log.d("MainActivity", "Seeding successful")
                isSeeding = false 
            }
            .addOnFailureListener { e -> 
                Log.e("MainActivity", "Seeding failed", e)
                isSeeding = false 
            }
    }

    private fun listenToPledges() {
        pledgesListener?.remove()
        pledgesListener = db.collection("pledges")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val pledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                val totalAmount = pledges.sumOf { it.amount }
                
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                binding.tvTotalPledged.text = currencyFormat.format(totalAmount)

                if (pledges.isNotEmpty()) {
                    binding.recentPledgesHeader.visibility = View.VISIBLE
                    binding.rvRecentPledges.visibility = View.VISIBLE
                    recentPledgesAdapter.submitList(pledges.take(5))
                } else {
                    binding.recentPledgesHeader.visibility = View.GONE
                    binding.rvRecentPledges.visibility = View.GONE
                }
            }
    }

    private fun showAdminOptions(need: Need) {
        val options = arrayOf(getString(R.string.mark_completed), getString(R.string.delete))
        AlertDialog.Builder(this)
            .setTitle(R.string.admin_options)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        pendingNeedForPhoto = need
                        pickAfterImage.launch("image/*")
                    }
                    1 -> deleteNeed(need)
                }
            }
            .show()
    }

    private fun uploadAfterImage(need: Need, uri: Uri) {
        val ref = storage.reference.child("after_photos/${UUID.randomUUID()}")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    markAsCompleted(need, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.photo_upload_failed), Toast.LENGTH_SHORT).show()
            }
    }

    private fun markAsCompleted(need: Need, afterImageUrl: String) {
        db.collection("needs").document(need.firebaseId)
            .update(mapOf(
                "status" to "COMPLETED",
                "afterImageUrl" to afterImageUrl
            ))
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.project_marked_completed), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteNeed(need: Need) {
        db.collection("needs").document(need.firebaseId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.need_deleted), Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPledgeDialog(need: Need) {
        val dialogBinding = DialogPledgeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.pledge_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.pledge_dialog_positive, null)
            .setNegativeButton(R.string.pledge_dialog_negative, null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.etDonorName.text.toString().trim()
            val amountStr = dialogBinding.etAmount.text.toString().trim()
            
            if (name.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    performPledge(need, name, amount)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performPledge(need: Need, donorName: String, amount: Double) {
        val newAmount = need.currentAmount + amount
        val updates = mutableMapOf<String, Any>("currentAmount" to newAmount)
        
        if (newAmount >= need.estimatedCost) {
            updates["status"] = "PLEDGED"
        }

        db.collection("needs").document(need.firebaseId).update(updates)
            .addOnSuccessListener {
                val pledge = Pledge(
                    needId = need.firebaseId,
                    needTitle = need.title,
                    donorName = donorName,
                    amount = amount
                )
                db.collection("pledges").add(pledge)
                Toast.makeText(this, getString(R.string.thank_you_donor, donorName), Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.pledge_failed, e.message), Toast.LENGTH_SHORT).show()
            }
    }
}
