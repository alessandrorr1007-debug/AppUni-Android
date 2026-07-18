package com.example.agendapx.ui.notas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.agendapx.data.AppConstants
import com.example.agendapx.data.NetworkUtils
import com.example.agendapx.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class NotasSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = inputData.getString("user_id") ?: return@withContext Result.failure()
            val token = UserPreferences.getToken()
            if (token.isEmpty()) return@withContext Result.failure()

            val prefsName = "${AppConstants.PREFS_NOTAS_CACHE}_${userId}"
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val cacheActual = prefs.getString(AppConstants.KEY_NOTAS_JSON, null)

            val respuesta = NetworkUtils.hacerGet("${AppConstants.BACKEND_URL}/notas", token)
            val obj = JSONObject(respuesta)

            if (!obj.optBoolean("ok", false)) return@withContext Result.retry()

            val data = obj.optJSONArray("data") ?: JSONArray()
            if (data.length() == 0) return@withContext Result.retry()

            val cambios = NotasNotificacionHelper.detectarCambios(cacheActual, respuesta)

            if (cambios.isNotEmpty()) {
                prefs.edit().putString(AppConstants.KEY_NOTAS_JSON, respuesta).apply()
                NotasNotificacionHelper.notificarCambios(context, cambios)
            } else if (cacheActual == null) {
                prefs.edit().putString(AppConstants.KEY_NOTAS_JSON, respuesta).apply()
            }

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}
