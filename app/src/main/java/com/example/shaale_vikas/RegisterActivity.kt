package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shaale_vikas.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.password_too_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (!isValid) return

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                val userId = user?.uid ?: ""
                
                // Update Firebase Auth Profile with Name
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                
                user?.updateProfile(profileUpdates)

                val userMap = hashMapOf(
                    "uid" to userId,
                    "name" to name,
                    "email" to email,
                    "role" to "ALUMNI"
                )

                db.collection("users").document(userId).set(userMap)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        handleError(e)
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                handleError(e)
            }
    }

    private fun handleError(e: Exception) {
        val message = when (e) {
            is FirebaseNetworkException -> getString(R.string.error_network)
            is FirebaseAuthUserCollisionException -> getString(R.string.error_email_collision)
            else -> getString(R.string.registration_failed, e.localizedMessage)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "" else getString(R.string.register_button)
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        binding.etName.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvGoToLogin.isEnabled = !isLoading
    }
}
