package com.example.agendapx

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.agendapx.data.AppConstants
import com.example.agendapx.data.NetworkUtils
import com.example.agendapx.data.ThemeManager
import com.example.agendapx.data.UserPreferences
import com.example.agendapx.databinding.ActivityMainBinding
import com.example.agendapx.ui.horario.HorarioFragment
import com.example.agendapx.ui.inicio.InicioFragment
import com.example.agendapx.ui.login.LoginActivity
import com.example.agendapx.ui.notas.NotasFragment
import com.example.agendapx.ui.notas.NotasSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreferences.init(this)

        lifecycleScope.launch {
            val themeMode = withContext(Dispatchers.IO) { UserPreferences.getThemeMode() }
            ThemeManager.setTheme(themeMode)

            withContext(Dispatchers.Main) {
                super@MainActivity.onCreate(savedInstanceState)

                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)

                currentUserId = withContext(Dispatchers.IO) {
                    UserPreferences.getCurrentUserId()
                }

                if (currentUserId.isEmpty()) {
                    navigateToLogin()
                    return@withContext
                }

                solicitarPermisoNotificaciones()
                programarSincronizacionPeriodica()
                cambiarFragment(InicioFragment())
                seleccionarTab(0)
                setupNavigation()
            }
        }
    }

    private fun setupNavigation() {
        binding.navInicio.setOnClickListener {
            cambiarFragment(InicioFragment())
            seleccionarTab(0)
        }

        binding.navHorario.setOnClickListener {
            cambiarFragment(HorarioFragment())
            seleccionarTab(1)
        }

        binding.navNotas.setOnClickListener {
            cambiarFragment(NotasFragment())
            seleccionarTab(2)
        }

        binding.btnCerrarSesion.setOnClickListener {
            confirmarCerrarSesion()
        }
    }

    private fun programarSincronizacionPeriodica() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = workDataOf("user_id" to currentUserId)

        val syncRequest = PeriodicWorkRequestBuilder<NotasSyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notas_sync_$currentUserId",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorPrincipal, fragment)
            .commit()
    }

    private fun seleccionarTab(posicion: Int) {
        binding.navInicio.setBackgroundResource(android.R.color.transparent)
        binding.navHorario.setBackgroundResource(android.R.color.transparent)
        binding.navNotas.setBackgroundResource(android.R.color.transparent)

        when (posicion) {
            0 -> binding.navInicio.setBackgroundResource(R.drawable.bg_nav_active)
            1 -> binding.navHorario.setBackgroundResource(R.drawable.bg_nav_active)
            2 -> binding.navNotas.setBackgroundResource(R.drawable.bg_nav_active)
        }
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val token = UserPreferences.getToken()
                    NetworkUtils.hacerPostAuth(
                        "${AppConstants.BACKEND_URL}/auth/logout",
                        org.json.JSONObject()
                    )
                } catch (e: Exception) {
                    Log.w("MainActivity", "Error al cerrar sesión en backend: ${e.message}")
                }

                UserPreferences.clearSession()
            }

            WorkManager.getInstance(this@MainActivity)
                .cancelUniqueWork("notas_sync_$currentUserId")

            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
