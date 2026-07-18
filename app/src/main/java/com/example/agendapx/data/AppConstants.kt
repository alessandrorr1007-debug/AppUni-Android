package com.example.agendapx.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppConstants {
    const val BACKEND_URL = "https://upao-px-backend.onrender.com"
    const val PREFS_NOTAS_CACHE = "notas_cache"
    const val KEY_NOTAS_JSON = "ultima_respuesta_notas"
    const val SEMESTER_START_YEAR = 2026
    const val SEMESTER_START_MONTH = Calendar.JUNE
    const val SEMESTER_START_DAY = 1
    const val SEMESTER_START_WEEK = 9

    fun obtenerDiaActual(): String {
        val formato = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES"))
        val dia = formato.format(Date()).replaceFirstChar { it.uppercase() }
        return when (dia) {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado" -> dia
            else -> "Lunes"
        }
    }

    fun calcularSemanaAcademica(): Int {
        val inicio = Calendar.getInstance().apply {
            set(SEMESTER_START_YEAR, SEMESTER_START_MONTH, SEMESTER_START_DAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val hoy = Calendar.getInstance()
        val diffMillis = hoy.timeInMillis - inicio.timeInMillis
        val semanas = (diffMillis / (1000 * 60 * 60 * 24 * 7)).toInt()
        return SEMESTER_START_WEEK + semanas
    }
}
