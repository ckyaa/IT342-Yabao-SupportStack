package com.example.supportstack

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supportstack.data.api.RetrofitClient
import com.example.supportstack.data.model.RegisterRequest
import com.example.supportstack.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            validateAndRegister()
        }

        binding.btnBackLanding.setOnClickListener {
            finish()
        }

        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateAndRegister() {
        // Clear previous errors
        binding.tilUsername.error = null
        binding.tilRegEmail.error = null
        binding.tilRegPassword.error = null
        binding.tilConfirmPassword.error = null

        // Get input values
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etRegEmail.text.toString().trim()
        val password = binding.etRegPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilRegEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegEmail.error = "Invalid email format"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilRegPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilRegPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (!isValid) return

        // Proceed with registration API call
        registerUser(username, email, password, confirmPassword)
    }

    private fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(username, email, password, confirmPassword)
                val response = RetrofitClient.apiService.register(request)

                setLoading(false)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        // Registration successful
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to home activity
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        // API returned error
                        val errorMessage = apiResponse?.error?.message ?: "Registration failed"
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    // HTTP error
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${response.code()} - ${errorBody ?: "Registration failed"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@RegisterActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnCreateAccount.isEnabled = !isLoading
        binding.btnCreateAccount.text = if (isLoading) "Creating Account..." else "Create Account"
        
        // Optionally disable input fields during loading
        binding.etUsername.isEnabled = !isLoading
        binding.etRegEmail.isEnabled = !isLoading
        binding.etRegPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
