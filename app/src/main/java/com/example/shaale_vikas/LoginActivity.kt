package com.example.shaale_vikas

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shaale_vikas.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-login if user is already authenticated
        if (auth.currentUser != null) {
            startMainActivity()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        var isValid = true

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
        } else {
            binding.tilPassword.error = null
        }

        if (!isValid) return

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                startMainActivity()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                handleLoginError(e)
            }
    }

    private fun handleLoginError(e: Exception) {
        val message = when (e) {
            is FirebaseNetworkException -> getString(R.string.error_network)
            is FirebaseAuthInvalidUserException -> getString(R.string.error_user_not_found)
            is FirebaseAuthInvalidCredentialsException -> getString(R.string.error_invalid_credentials)
            else -> getString(R.string.login_failed, e.localizedMessage)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else getString(R.string.login_button)
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvGoToRegister.isEnabled = !isLoading
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
