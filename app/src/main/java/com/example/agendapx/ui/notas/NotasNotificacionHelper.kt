package com.example.agendapx.ui.notas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject

/**
 * Helper para crear canales de notificación y disparar alertas de cambios de notas.
 * Compara el JSON anterior con el nuevo para evitar notificaciones repetidas.
 */
object NotasNotificacionHelper {

    private const val CHANNEL_ID = "notas_cambios"
    private const val CHANNEL_NAME = "Cambios de Notas UPAO"
    private const val CHANNEL_DESC = "Notificaciones cuando tus notas son actualizadas"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }
    }

    /**
     * Compara el JSON anterior con el nuevo y retorna los cambios detectados.
     * Detecta: nota nueva, nota modificada, cambio de promedio.
     */
    fun detectarCambios(jsonAnterior: String?, jsonNuevo: String): List<String> {
        if (jsonAnterior.isNullOrBlank()) return emptyList()

        val cambios = mutableListOf<String>()

        return try {
            val dataAnterior = extraerCursos(jsonAnterior)
            val dataNueva = extraerCursos(jsonNuevo)

            for (i in 0 until dataNueva.length()) {
                val cursoNuevo = dataNueva.getJSONObject(i)
                val nrc = cursoNuevo.optString("nrc", "")
                val nombre = cursoNuevo.optString("course", "Curso")

                // Buscar el curso correspondiente en el JSON anterior
                val cursoAnterior = buscarCursoPorNrc(dataAnterior, nrc)

                if (cursoAnterior == null) {
                    // Curso nuevo
                    cambios.add("📊 Nuevo curso registrado: $nombre")
                    continue
                }

                // Comparar componentes
                listOf("ep1", "parcial", "ep2", "final").forEach { comp ->
                    val puntajeAnterior = obtenerPuntaje(cursoAnterior, comp)
                    val puntajeNuevo = obtenerPuntaje(cursoNuevo, comp)

                    if (puntajeAnterior != puntajeNuevo && puntajeNuevo.isNotBlank()) {
                        val etiqueta = comp.uppercase()
                        cambios.add("📊 $nombre · $etiqueta actualizado: $puntajeNuevo/20")
                    }
                }
            }

            cambios
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun notificarCambios(context: Context, cambios: List<String>) {
        if (cambios.isEmpty()) return

        crearCanal(context)

        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        cambios.forEachIndexed { index, mensaje ->
            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Notas actualizadas")
                .setContentText(mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            // ID único por notificación para que no se sobreescriban si hay varios cambios
            manager.notify(System.currentTimeMillis().toInt() + index, notif)
        }
    }

    private fun extraerCursos(jsonTexto: String): JSONArray {
        return JSONObject(jsonTexto).optJSONArray("data") ?: JSONArray()
    }

    private fun buscarCursoPorNrc(array: JSONArray, nrc: String): JSONObject? {
        for (i in 0 until array.length()) {
            val curso = array.getJSONObject(i)
            if (curso.optString("nrc") == nrc) return curso
        }
        return null
    }

    private fun obtenerPuntaje(curso: JSONObject, clave: String): String {
        return curso.optJSONObject(clave)?.optString("puntaje", "") ?: ""
    }
}
