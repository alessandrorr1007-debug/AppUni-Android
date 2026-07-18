package com.example.agendapx.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agendapx.MainActivity
import com.example.agendapx.data.AppConstants
import com.example.agendapx.data.NetworkUtils
import com.example.agendapx.data.ThemeManager
import com.example.agendapx.data.UserPreferences
import com.example.agendapx.databinding.ActivityLoginBinding
import com.example.agendapx.data.update.UpdateChecker
import com.example.agendapx.data.update.UpdateDialogActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val backendBase = AppConstants.BACKEND_URL

    private val mensajesCarga = listOf(
        "Verificando credenciales...",
        "Conectando con UPAO...",
        "Autenticando en el portal...",
        "Cargando tu información académica...",
        "Casi listo..."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreferences.init(this)

        lifecycleScope.launch {
            val themeMode = withContext(Dispatchers.IO) { UserPreferences.getThemeMode() }
            ThemeManager.setTheme(themeMode)

            withContext(Dispatchers.Main) {
                super@LoginActivity.onCreate(savedInstanceState)

                binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)

                mostrarEstado("formulario")

                checkExistingSession()
                checkForUpdates()
                setupListeners()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Sesión existente
    // ─────────────────────────────────────────────────────────────────

    private fun checkExistingSession() {
        lifecycleScope.launch {
            val isLoggedIn = UserPreferences.isLoggedIn()
            val rememberSession = UserPreferences.isRememberSessionEnabled()

            if (isLoggedIn && rememberSession) {
                val userId = UserPreferences.getCurrentUserId()
                val token = UserPreferences.getToken()
                if (userId.isNotEmpty() && token.isNotEmpty()) {
                    mostrarEstado("cargando", "Verificando sesión guardada...")

                    val sesionValida = withContext(Dispatchers.IO) {
                        verificarSesionBackend(token)
                    }

                    if (sesionValida) {
                        navigateToMain()
                    } else {
                        withContext(Dispatchers.IO) { UserPreferences.clearSession() }
                        mostrarEstado("formulario")
                        mostrarError("Tu sesión expiró. Por favor vuelve a ingresar.")
                    }
                } else {
                    mostrarEstado("formulario")
                }
            }
        }
    }

    private fun verificarSesionBackend(token: String): Boolean {
        return try {
            val response = NetworkUtils.hacerGet("$backendBase/auth/status", token)
            val json = JSONObject(response)
            json.optBoolean("authenticated", false)
        } catch (e: Exception) {
            true
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Actualización
    // ─────────────────────────────────────────────────────────────────

    private fun checkForUpdates() {
        lifecycleScope.launch {
            val info = UpdateChecker.checkForUpdate(this@LoginActivity)
            if (info != null) {
                startActivity(UpdateDialogActivity.createIntent(this@LoginActivity, info))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Formulario
    // ─────────────────────────────────────────────────────────────────

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val userId = binding.edtUserId.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val remember = binding.swRecordarSesion.isChecked

            if (userId.isEmpty()) {
                mostrarError("Ingresa tu ID de alumno")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                mostrarError("Ingresa tu contraseña")
                return@setOnClickListener
            }

            performLogin(userId, password, remember)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Login
    // ─────────────────────────────────────────────────────────────────

    private fun performLogin(userId: String, password: String, remember: Boolean) {
        mostrarEstado("cargando", mensajesCarga[0])
        iniciarRotacionMensajes()

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
                        val token = json.optString("token", "")
                        LoginResult.Success(token)
                    } else {
                        val msg = json.optString("message", "Error al iniciar sesión")
                        LoginResult.Error(msg)
                    }
                } catch (e: Exception) {
                    LoginResult.Error("No se pudo conectar con el servidor")
                }
            }

            when (result) {
                is LoginResult.Success -> {
                    UserPreferences.saveSession(userId, userId, remember, result.token)
                    navigateToMain()
                }
                is LoginResult.Error -> {
                    mostrarEstado("formulario")
                    mostrarError(result.message)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Estados de UI
    // ─────────────────────────────────────────────────────────────────

    private fun mostrarEstado(estado: String, mensaje: String = "") {
        when (estado) {
            "formulario" -> {
                binding.seccionFormulario.visibility = View.VISIBLE
                binding.seccionCargando.visibility = View.GONE
                binding.txtError.visibility = View.GONE
            }
            "cargando" -> {
                binding.seccionFormulario.visibility = View.GONE
                binding.seccionCargando.visibility = View.VISIBLE
                binding.txtMensajeCarga.text = mensaje
                iniciarAnimacionLogo()
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        binding.txtError.text = mensaje
        binding.txtError.visibility = View.VISIBLE
    }

    // ─────────────────────────────────────────────────────────────────
    //  Animaciones de carga
    // ─────────────────────────────────────────────────────────────────

    private var mensajeIndex = 0
    private var rotandoMensajes = false

    private fun iniciarRotacionMensajes() {
        rotandoMensajes = true
        mensajeIndex = 0

        lifecycleScope.launch {
            while (rotandoMensajes && binding.seccionCargando.visibility == View.VISIBLE) {
                delay(4000)
                if (!rotandoMensajes) break
                mensajeIndex = (mensajeIndex + 1) % mensajesCarga.size
                binding.txtMensajeCarga.text = mensajesCarga[mensajeIndex]
            }
        }
    }

    private fun iniciarAnimacionLogo() {
        val scaleX = ObjectAnimator.ofFloat(binding.txtLogoEmoji, "scaleX", 1f, 1.15f, 1f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(binding.txtLogoEmoji, "scaleY", 1f, 1.15f, 1f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val animSet = AnimatorSet()
        animSet.playTogether(scaleX, scaleY)
        animSet.duration = 1500
        animSet.interpolator = AccelerateDecelerateInterpolator()
        animSet.start()
    }

    // ─────────────────────────────────────────────────────────────────
    //  Navegación
    // ─────────────────────────────────────────────────────────────────

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        rotandoMensajes = false
    }

    private sealed class LoginResult {
        data class Success(val token: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
