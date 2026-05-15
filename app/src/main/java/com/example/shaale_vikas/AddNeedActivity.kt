package com.example.shaale_vikas

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.shaale_vikas.databinding.ActivityAddNeedBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddNeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNeedBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null
    private var isSubmitting = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivSelectedImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()

        binding.ivSelectedImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            
            if (selectedImageUri != null) {
                uploadImageAndSaveNeed()
            } else {
                saveNeed(null)
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Infrastructure", "Learning Materials", "Sports", "Sanitation", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actCategory.setAdapter(adapter)
    }

    private fun uploadImageAndSaveNeed() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_title_error), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val ref = storage.reference.child("needs/${UUID.randomUUID()}")
        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveNeed(uri.toString())
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveNeed(imageUrl: String?) {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val costString = binding.etCost.text.toString().trim()
        val category = binding.actCategory.text.toString()

        if (title.isEmpty() || description.isEmpty() || costString.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        if (!isSubmitting) setLoading(true)

        val cost = costString.toDoubleOrNull() ?: 0.0
        
        val newNeed = Need(
            title = title,
            description = description,
            category = category,
            estimatedCost = cost,
            currentAmount = 0.0,
            imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1580582932707-520aed937b7b?q=80&w=1000&auto=format&fit=crop",
            beforeImageUrl = imageUrl ?: ""
        )

        db.collection("needs").add(newNeed)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.post_success), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, getString(R.string.post_failed), Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        isSubmitting = loading
        binding.btnSubmit.isEnabled = !loading
        binding.btnSubmit.text = if (loading) getString(R.string.uploading_dots) else getString(R.string.post_need)
    }
}
