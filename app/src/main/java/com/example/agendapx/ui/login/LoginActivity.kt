package com.example.agendapx.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agendapx.MainActivity
import com.example.agendapx.data.NetworkUtils
import com.example.agendapx.data.UserPreferences
import com.example.agendapx.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val backendBase = "https://upao-px-backend.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPreferences.init(this)

        checkExistingSession()
        setupListeners()
    }

    private fun checkExistingSession() {
        lifecycleScope.launch {
            val isLoggedIn = UserPreferences.isLoggedIn.firstOrNull() ?: false
            val rememberSession = UserPreferences.isRememberSessionEnabled()

            if (isLoggedIn && rememberSession) {
                navigateToMain()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val userId = binding.edtUserId.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val remember = binding.swRecordarSesion.isChecked

            if (userId.isEmpty()) {
                showError("Ingresa tu ID de alumno")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showError("Ingresa tu contraseña")
                return@setOnClickListener
            }

            performLogin(userId, password, remember)
        }
    }

    private fun performLogin(userId: String, password: String, remember: Boolean) {
        binding.btnLogin.isEnabled = false
        binding.txtError.visibility = View.GONE

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val body = JSONObject().apply {
                        put("usuario", userId)
                        put("password", password)
                        put("remember", remember)
                    }

                    val response = NetworkUtils.hacerPost("$backendBase/auth/login", body)
                    val json = JSONObject(response)

                    if (json.optBoolean("ok", false)) {
                        LoginResult.Success
                    } else {
                        val msg = json.optString("message", "Error al iniciar sesión")
                        LoginResult.Error(msg)
                    }
                } catch (e: Exception) {
                    LoginResult.Error("Error de conexión con el servidor")
                }
            }

            when (result) {
                is LoginResult.Success -> {
                    val userName = userId
                    UserPreferences.saveSession(userId, userName, remember)
                    Toast.makeText(
                        this@LoginActivity,
                        "Sesión iniciada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }
                is LoginResult.Error -> {
                    showError(result.message)
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.txtError.text = message
        binding.txtError.visibility = View.VISIBLE
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private sealed class LoginResult {
        data object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
