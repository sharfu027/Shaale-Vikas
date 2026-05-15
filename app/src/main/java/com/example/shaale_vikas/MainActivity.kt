package com.example.shaale_vikas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NeedsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var needsListener: ListenerRegistration? = null
    
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
        setupRecyclerView()

        binding.fabAddNeed.setOnClickListener {
            startActivity(Intent(this, AddNeedActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        startListening()
    }

    override fun onStop() {
        super.onStop()
        needsListener?.remove()
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

    private fun setupRecyclerView() {
        adapter = NeedsAdapter(
            onPledgeClick = { need -> showPledgeDialog(need) },
            onLongClick = { need -> showAdminOptions(need) }
        )
        binding.rvNeeds.layoutManager = LinearLayoutManager(this)
        binding.rvNeeds.adapter = adapter
    }

    private fun startListening() {
        needsListener?.remove()
        needsListener = db.collection("needs")
            .orderBy("status", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, getString(R.string.error_fetching_needs, error.message), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val needsList = value?.toObjects(Need::class.java) ?: emptyList()
                
                if (needsList.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.rvNeeds.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.rvNeeds.visibility = View.VISIBLE
                }
                
                adapter.submitList(needsList)
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
