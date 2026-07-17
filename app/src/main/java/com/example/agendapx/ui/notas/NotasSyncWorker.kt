package com.example.agendapx.ui.notas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.agendapx.data.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Worker que se ejecuta en background cada 30 minutos.
 * Consulta /notas, compara con el caché local y lanza notificaciones si hay cambios.
 */
class NotasSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val backendUrl = "https://upao-px-backend.onrender.com/notas"
    private val PREFS_BASE = "notas_cache"
    private val KEY_JSON = "ultima_respuesta_notas"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = inputData.getString("user_id") ?: return@withContext Result.failure()
            val prefsName = "${PREFS_BASE}_${userId}"
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val cacheActual = prefs.getString(KEY_JSON, null)

            // 2. Consultar el backend
            val respuesta = hacerGet("$backendUrl?userId=$userId")
            val obj = JSONObject(respuesta)

            if (!obj.optBoolean("ok", false)) return@withContext Result.retry()

            val data = obj.optJSONArray("data") ?: JSONArray()
            if (data.length() == 0) return@withContext Result.retry()

            // 3. Detectar cambios respecto al caché
            val cambios = NotasNotificacionHelper.detectarCambios(cacheActual, respuesta)

            // 4. Si hay cambios: actualizar caché y notificar
            if (cambios.isNotEmpty()) {
                prefs.edit().putString(KEY_JSON, respuesta).apply()
                NotasNotificacionHelper.notificarCambios(context, cambios)
            } else if (cacheActual == null) {
                // Primera vez: guardar sin notificar
                prefs.edit().putString(KEY_JSON, respuesta).apply()
            }

            Result.success()

        } catch (e: Exception) {
            // Sin conexión o error de red → reintentar después
            Result.retry()
        }
    }

    private fun hacerGet(urlTexto: String): String = NetworkUtils.hacerGet(urlTexto)
}
