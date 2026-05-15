package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shaale_vikas.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
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
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is FirebaseAuthUserCollisionException -> "This email is already registered."
            else -> "Error: ${e.localizedMessage}. Please ensure Email/Password auth is enabled in Firebase Console."
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) getString(R.string.registering) else getString(R.string.register_button)
    }
}
