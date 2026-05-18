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
import com.google.android.material.chip.Chip
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
    private var currentUserRole: String = "ALUMNI"
    private var selectedCategory: String = "All"
    private var pendingNeedForPhoto: Need? = null
    private val pickAfterImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && pendingNeedForPhoto != null) {
            uploadAfterImage(pendingNeedForPhoto!!, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerViews()
        setupCategoryFilters()
        fetchUserRole()

        binding.fabAddNeed.setOnClickListener {
            startActivity(Intent(this, AddNeedActivity::class.java))
        }
    }

    private fun setupCategoryFilters() {
        val categories = listOf("All", "Infrastructure", "Learning Materials", "Sports", "Sanitation", "Others")
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = category == "All"
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category
                        startListening()
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentUserRole = document.getString("role") ?: "ALUMNI"
                    updateUIForRole()
                }
            }
    }

    private fun updateUIForRole() {
        val isAdmin = currentUserRole == "ADMIN"
        binding.fabAddNeed.visibility = if (isAdmin) View.VISIBLE else View.GONE
        invalidateOptionsMenu()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startListening()
            listenToPledges()
        }
    }

    override fun onStop() {
        super.onStop()
        needsListener?.remove()
        pledgesListener?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu?.findItem(R.id.action_admin_dashboard)?.isVisible = (currentUserRole == "ADMIN")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hall_of_fame -> {
                startActivity(Intent(this, DonorsActivity::class.java))
                true
            }
            R.id.action_success_stories -> {
                startActivity(Intent(this, SuccessStoriesActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_admin_dashboard -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
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
        navigateToLogin()
    }

    private fun setupRecyclerViews() {
        adapter = NeedsAdapter(
            onPledgeClick = { need -> 
                val intent = Intent(this, PledgeActivity::class.java).apply {
                    putExtra("NEED_ID", need.firebaseId)
                    putExtra("NEED_TITLE", need.title)
                }
                startActivity(intent)
            },
            onLongClick = { need -> 
                if (currentUserRole == "ADMIN") {
                    showAdminOptions(need)
                }
            }
        )
        binding.rvNeeds.layoutManager = LinearLayoutManager(this)
        binding.rvNeeds.adapter = adapter

        recentPledgesAdapter = RecentPledgesAdapter()
        binding.rvRecentPledges.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentPledges.adapter = recentPledgesAdapter
    }

    private fun startListening() {
        needsListener?.remove()
        
        var query: Query = db.collection("needs")
        if (selectedCategory != "All") {
            query = query.whereEqualTo("category", selectedCategory)
        }

        needsListener = query.addSnapshotListener { value, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    return@addSnapshotListener
                }

                val needsList = value?.toObjects(Need::class.java) ?: emptyList()
                
                if (needsList.isEmpty() && !isSeeding && currentUserRole == "ADMIN" && selectedCategory == "All") {
                    seedDefaultData()
                }
                
                updateVisibility(needsList)
                binding.tvActiveNeeds.text = needsList.count { it.status != "COMPLETED" }.toString()
                
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
            Toast.makeText(this, "Permission Denied! Check your Firestore rules.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seedDefaultData() {
        isSeeding = true
        val need1 = Need(
            title = "New Library Books",
            description = "Providing updated science and literature books for students.",
            category = "Learning Materials",
            estimatedCost = 15000.0,
            imageUrl = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=1000"
        )
        val need2 = Need(
            title = "Clean Drinking Water",
            description = "Installation of a new RO water filtration system.",
            category = "Sanitation",
            estimatedCost = 25000.0,
            imageUrl = "https://images.unsplash.com/photo-1548936236-442d35a09804?q=80&w=1000"
        )

        val batch = db.batch()
        batch.set(db.collection("needs").document(), need1)
        batch.set(db.collection("needs").document(), need2)
        
        batch.commit().addOnCompleteListener { isSeeding = false }
    }

    private fun listenToPledges() {
        pledgesListener?.remove()
        pledgesListener = db.collection("pledges")
            .whereEqualTo("status", "APPROVED")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("MainActivity", "Pledges Error: ${error.message}")
                    return@addSnapshotListener
                }
                
                val pledges = value?.toObjects(Pledge::class.java) ?: emptyList()
                val totalAmount = pledges.sumOf { it.amount }
                
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                binding.tvTotalPledged.text = currencyFormat.format(totalAmount)

                if (pledges.isNotEmpty()) {
                    binding.recentPledgesHeader.visibility = View.VISIBLE
                    binding.rvRecentPledges.visibility = View.VISIBLE
                    recentPledgesAdapter.submitList(pledges.take(10))
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
}
