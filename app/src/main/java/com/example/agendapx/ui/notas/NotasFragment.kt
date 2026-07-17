package com.example.agendapx.ui.notas

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.agendapx.R
import com.example.agendapx.data.NetworkUtils
import com.example.agendapx.data.UserPreferences
import com.example.agendapx.databinding.FragmentNotasBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class NotasFragment : Fragment() {

    private var _binding: FragmentNotasBinding? = null
    private val binding get() = _binding!!

    private val backendUrl = "https://upao-px-backend.onrender.com/notas"
    private val PREFS_BASE = "notas_cache"
    private val KEY_JSON = "ultima_respuesta_notas"

    private val cursosJson = mutableListOf<JSONObject>()
    private var userId: String = ""

    // ─────────────────────────────────────────────────────────────────────────
    //  Ciclo de vida
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        NotasNotificacionHelper.crearCanal(requireContext())

        lifecycleScope.launch {
            userId = withContext(Dispatchers.IO) {
                UserPreferences.getCurrentUserId()
            }

            activarEventos()

            val hayCache = cargarDesdeCache()
            cargarDesdeBackend(force = false, mostrarCargando = !hayCache)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Eventos
    // ─────────────────────────────────────────────────────────────────────────

    private fun activarEventos() {
        binding.btnActualizarNotas.setOnClickListener {
            Toast.makeText(requireContext(), "Actualizando desde UPAO PX...", Toast.LENGTH_SHORT).show()
            cargarDesdeBackend(force = true, mostrarCargando = true)
        }

        binding.btnVolverCursos.setOnClickListener {
            mostrarListaCursos()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Caché (SharedPreferences)
    // ─────────────────────────────────────────────────────────────────────────

    private fun cargarDesdeCache(): Boolean {
        val prefsName = "${PREFS_BASE}_${userId}"
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val cache = prefs.getString(KEY_JSON, null)

        if (cache.isNullOrBlank()) {
            binding.txtEstadoGeneralNotas.text = "Cargando notas desde UPAO PX..."
            return false
        }

        return try {
            val obj = JSONObject(cache)
            val data = obj.optJSONArray("data") ?: JSONArray()

            if (data.length() == 0) return false

            procesarYRenderizar(cache, desdeCache = true, jsonAnterior = null)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun guardarEnCache(jsonTexto: String) {
        val prefsName = "${PREFS_BASE}_${userId}"
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_JSON, jsonTexto).apply()
    }

    private fun leerCache(): String? {
        val prefsName = "${PREFS_BASE}_${userId}"
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return prefs.getString(KEY_JSON, null)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Red
    // ─────────────────────────────────────────────────────────────────────────

    private fun cargarDesdeBackend(force: Boolean, mostrarCargando: Boolean) {
        if (mostrarCargando) {
            binding.progressBarNotas.visibility = View.VISIBLE
            binding.txtEstadoGeneralNotas.text = "Sincronizando con UPAO PX..."
        }

        thread {
            try {
                val separator = if (force) "&" else "?"
                val url = "$backendUrl${separator}userId=$userId${if (force) "&force=true" else ""}"
                val respuesta = hacerGet(url)
                val obj = JSONObject(respuesta)
                val data = obj.optJSONArray("data") ?: JSONArray()

                if (!obj.optBoolean("ok", false) || data.length() == 0) {
                    requireActivity().runOnUiThread { fallback() }
                    return@thread
                }

                // Detectar cambios antes de sobrescribir el caché
                val cacheAnterior = leerCache()
                val cambios = NotasNotificacionHelper.detectarCambios(cacheAnterior, respuesta)

                // Guardar nuevo caché
                guardarEnCache(respuesta)

                requireActivity().runOnUiThread {
                    binding.progressBarNotas.visibility = View.GONE
                    procesarYRenderizar(respuesta, desdeCache = false, jsonAnterior = cacheAnterior)

                    // Notificar cambios detectados
                    if (cambios.isNotEmpty()) {
                        NotasNotificacionHelper.notificarCambios(requireContext(), cambios)
                    }
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    binding.progressBarNotas.visibility = View.GONE
                    fallback()
                    if (cursosJson.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Sin conexión. Se muestran las notas guardadas.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun hacerGet(urlTexto: String): String = NetworkUtils.hacerGet(urlTexto)

    private fun fallback() {
        if (cursosJson.isNotEmpty()) {
            binding.txtEstadoGeneralNotas.text = "📶 Sin conexión · Mostrando última información guardada"
        } else {
            cargarDesdeCache()
            if (cursosJson.isEmpty()) {
                binding.txtEstadoGeneralNotas.text = "Sin notas guardadas. Verifica conexión."
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Procesamiento y Renderizado
    // ─────────────────────────────────────────────────────────────────────────

    private fun procesarYRenderizar(
        jsonTexto: String,
        desdeCache: Boolean,
        jsonAnterior: String?
    ) {
        val obj = JSONObject(jsonTexto)
        val data = obj.optJSONArray("data") ?: return
        val updatedAt = obj.optString("updatedAt", "")

        cursosJson.clear()
        for (i in 0 until data.length()) {
            cursosJson.add(data.getJSONObject(i))
        }

        // KPIs
        val promedioGeneral = cursosJson
            .map { calcularPromedio(it).toDouble() }
            .average()
            .toFloat()
            .coerceIn(0f, 20f)

        binding.txtCursosRegistrados.text = cursosJson.size.toString()
        binding.txtPromedioGeneralNotas.text = "%.2f".format(promedioGeneral)

        // Estado
        binding.txtEstadoGeneralNotas.text = when {
            updatedAt.isNotBlank() && desdeCache -> "💾 Guardado · ${formatearFecha(updatedAt)}"
            updatedAt.isNotBlank() -> "✅ Actualizado · ${formatearFecha(updatedAt)}"
            desdeCache -> "💾 Mostrando última actualización guardada"
            else -> "✅ Notas cargadas desde UPAO PX"
        }

        mostrarListaCursos()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Vista: Lista de Cursos
    // ─────────────────────────────────────────────────────────────────────────

    private fun mostrarListaCursos() {
        binding.seccionDetalleCurso.visibility = View.GONE
        binding.seccionCursos.visibility = View.VISIBLE
        binding.cardResumenNotas.visibility = View.VISIBLE
        binding.txtTituloNotas.text = "Mis Notas"
        binding.txtSubtituloNotas.text = "Toca un curso para ver el detalle"

        binding.contenedorCursos.removeAllViews()

        if (cursosJson.isEmpty()) {
            val txt = crearTexto(
                "No hay cursos disponibles.",
                14f,
                colorHex("#94A3B8")
            )
            binding.contenedorCursos.addView(txt)
            return
        }

        cursosJson.forEach { curso -> binding.contenedorCursos.addView(crearCardCurso(curso)) }
    }

    private fun crearCardCurso(curso: JSONObject): CardView {
        val nombre = curso.optString("course", "Curso")
        val nrc = curso.optString("nrc", "--")
        val creditos = curso.optString("creditos", "--")
        val promedio = calcularPromedio(curso)
        val aprobado = promedio >= 10.5f

        val card = CardView(requireContext())
        card.radius = dpToPx(20f)
        card.cardElevation = 0f
        card.setCardBackgroundColor(colorHex("#1A1A35"))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, dpToPx(12f).toInt())
        card.layoutParams = params

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(
            dpToPx(18f).toInt(), dpToPx(16f).toInt(),
            dpToPx(18f).toInt(), dpToPx(16f).toInt()
        )
        layout.gravity = android.view.Gravity.CENTER_VERTICAL

        // Indicador de color (barra lateral)
        val barra = View(requireContext())
        val barraParams = LinearLayout.LayoutParams(dpToPx(4f).toInt(), LinearLayout.LayoutParams.MATCH_PARENT)
        barraParams.setMargins(0, 0, dpToPx(14f).toInt(), 0)
        barra.layoutParams = barraParams
        barra.background = crearFondoColor(
            if (aprobado) "#22C55E" else if (promedio > 0f) "#F59E0B" else "#475569",
            dpToPx(2f)
        )

        // Contenido central
        val contenido = LinearLayout(requireContext())
        contenido.orientation = LinearLayout.VERTICAL
        contenido.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val txtNombre = crearTexto(nombre, 16f, colorHex("#F1F5F9"), bold = true)
        val txtMeta = crearTexto("NRC $nrc · $creditos créditos", 12f, colorHex("#64748B"))
        txtMeta.setPadding(0, 4, 0, 0)

        contenido.addView(txtNombre)
        contenido.addView(txtMeta)

        // Promedio + badge
        val derecha = LinearLayout(requireContext())
        derecha.orientation = LinearLayout.VERTICAL
        derecha.gravity = android.view.Gravity.CENTER_HORIZONTAL

        val txtPromedio = crearTexto(
            if (promedio > 0f) "%.2f".format(promedio) else "--",
            22f,
            if (aprobado) colorHex("#86EFAC") else if (promedio > 0f) colorHex("#FCD34D") else colorHex("#94A3B8"),
            bold = true
        )

        val txtEstado = crearTexto(
            if (aprobado) "✓ OK" else if (promedio > 0f) "⚠ Riesgo" else "Pendiente",
            11f,
            if (aprobado) colorHex("#22C55E") else if (promedio > 0f) colorHex("#F59E0B") else colorHex("#475569"),
            bold = true
        )
        txtEstado.setPadding(0, 2, 0, 0)

        derecha.addView(txtPromedio)
        derecha.addView(txtEstado)

        layout.addView(barra)
        layout.addView(contenido)
        layout.addView(derecha)
        card.addView(layout)

        card.setOnClickListener { abrirDetalleCurso(curso) }

        return card
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Vista: Detalle de Curso con Componentes y Subcomponentes
    // ─────────────────────────────────────────────────────────────────────────

    private fun abrirDetalleCurso(curso: JSONObject) {
        binding.seccionCursos.visibility = View.GONE
        binding.cardResumenNotas.visibility = View.GONE
        binding.seccionDetalleCurso.visibility = View.VISIBLE

        val nombre = curso.optString("course", "Curso")
        val nrc = curso.optString("nrc", "--")
        val creditos = curso.optString("creditos", "--")
        val promedio = calcularPromedio(curso)
        val aprobado = promedio >= 10.5f

        binding.txtTituloNotas.text = "Detalle"
        binding.txtSubtituloNotas.text = "Evaluaciones sincronizadas"
        binding.txtCursoSeleccionado.text = nombre
        binding.txtDescripcionCurso.text = "NRC $nrc · $creditos créditos"
        binding.txtPromedioCurso.text = "Promedio: ${if (promedio > 0f) "%.2f".format(promedio) else "--"}/20"
        binding.txtPromedioCurso.setTextColor(
            if (aprobado) colorHex("#86EFAC") else colorHex("#FCA5A5")
        )
        binding.progresoCurso.progress = (promedio * 100).roundToInt().coerceIn(0, 2000)
        binding.txtFinalNecesarioCurso.text = calcularFinalNecesario(curso)

        // Poblar componentes dinámicamente
        binding.contenedorComponentes.removeAllViews()

        listOf(
            Triple("ep1", "EP1", "20%"),
            Triple("parcial", "Parcial", "30%"),
            Triple("ep2", "EP2", "20%"),
            Triple("final", "Final", "30%")
        ).forEach { (clave, etiqueta, peso) ->
            val componente = curso.optJSONObject(clave)
            binding.contenedorComponentes.addView(
                crearCardComponente(etiqueta, peso, componente)
            )
        }

        binding.scrollNotas.post { binding.scrollNotas.smoothScrollTo(0, 0) }
    }

    private fun crearCardComponente(
        etiqueta: String,
        peso: String,
        componente: JSONObject?
    ): CardView {
        val puntaje = componente?.optString("puntaje", "") ?: ""
        val calificacion = componente?.optString("calificacion", "") ?: ""
        val subcomponentes = componente?.optJSONArray("subcomponentes") ?: JSONArray()
        val tieneNota = puntaje.isNotBlank()
        val nota = puntaje.replace(",", ".").toFloatOrNull() ?: 0f

        val card = CardView(requireContext())
        card.radius = dpToPx(18f)
        card.cardElevation = 0f
        card.setCardBackgroundColor(colorHex("#1A1A35"))

        val paramsCard = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        paramsCard.setMargins(0, 0, 0, dpToPx(10f).toInt())
        card.layoutParams = paramsCard

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        // ── Cabecera del componente ─────────────────────────────────────────
        val header = LinearLayout(requireContext())
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = android.view.Gravity.CENTER_VERTICAL
        header.setPadding(
            dpToPx(16f).toInt(), dpToPx(14f).toInt(),
            dpToPx(16f).toInt(), dpToPx(14f).toInt()
        )
        header.setBackgroundColor(colorHex("#20203F"))

        val txtEtiqueta = crearTexto("$etiqueta", 14f, colorHex("#F1F5F9"), bold = true)
        txtEtiqueta.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val txtPeso = crearTexto(peso, 12f, colorHex("#64748B"))

        val colorNota = when {
            !tieneNota -> colorHex("#475569")
            nota >= 10.5f -> colorHex("#86EFAC")
            nota >= 8f -> colorHex("#FCD34D")
            else -> colorHex("#FCA5A5")
        }
        val txtNota = crearTexto(
            if (tieneNota) "$puntaje/20" else "--",
            20f, colorNota, bold = true
        )
        txtNota.setPadding(dpToPx(12f).toInt(), 0, 0, 0)

        header.addView(txtEtiqueta)
        header.addView(txtPeso)
        header.addView(txtNota)
        layout.addView(header)

        // Calificación redondeada (si existe)
        if (calificacion.isNotBlank() && tieneNota) {
            val txtCal = crearTexto("Calificación redondeada: $calificacion", 12f, colorHex("#64748B"))
            txtCal.setPadding(
                dpToPx(16f).toInt(), dpToPx(4f).toInt(),
                dpToPx(16f).toInt(), dpToPx(4f).toInt()
            )
            layout.addView(txtCal)
        }

        // ── Subcomponentes ──────────────────────────────────────────────────
        if (subcomponentes.length() > 0) {
            val divider = View(requireContext())
            divider.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            divider.setBackgroundColor(colorHex("#1E1E3F"))
            layout.addView(divider)

            for (i in 0 until subcomponentes.length()) {
                val sub = subcomponentes.getJSONObject(i)
                val nombreSub = limpiarNombreSub(sub.optString("nombre", "Sub"))
                val puntajeSub = sub.optString("puntaje", "")
                val pesoSub = sub.optString("peso", "")
                val notaSub = puntajeSub.replace(",", ".").toFloatOrNull() ?: 0f
                val tieneNotaSub = puntajeSub.isNotBlank()

                val fila = LinearLayout(requireContext())
                fila.orientation = LinearLayout.HORIZONTAL
                fila.gravity = android.view.Gravity.CENTER_VERTICAL
                fila.setPadding(
                    dpToPx(16f).toInt(), dpToPx(10f).toInt(),
                    dpToPx(16f).toInt(), dpToPx(10f).toInt()
                )

                // Bullet point
                val bullet = crearTexto("•", 14f, colorHex("#4F46E5"))
                bullet.setPadding(0, 0, dpToPx(8f).toInt(), 0)

                val txtSubNombre = crearTexto(
                    nombreSub + if (pesoSub.isNotBlank()) " ($pesoSub%)" else "",
                    13f, colorHex("#94A3B8")
                )
                txtSubNombre.layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                val colorSub = when {
                    !tieneNotaSub -> colorHex("#475569")
                    notaSub >= 10.5f -> colorHex("#86EFAC")
                    notaSub >= 8f -> colorHex("#FCD34D")
                    else -> colorHex("#FCA5A5")
                }
                val txtSubNota = crearTexto(
                    if (tieneNotaSub) "$puntajeSub/20" else "--",
                    13f, colorSub, bold = true
                )

                fila.addView(bullet)
                fila.addView(txtSubNombre)
                fila.addView(txtSubNota)

                layout.addView(fila)

                // Separador entre subcomponentes (excepto el último)
                if (i < subcomponentes.length() - 1) {
                    val sep = View(requireContext())
                    sep.layoutParams =
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    sep.setBackgroundColor(colorHex("#1E1E3F"))
                    layout.addView(sep)
                }
            }
        } else if (!tieneNota) {
            val txtSin = crearTexto("Sin nota registrada", 12f, colorHex("#475569"))
            txtSin.setPadding(
                dpToPx(16f).toInt(), dpToPx(8f).toInt(),
                dpToPx(16f).toInt(), dpToPx(10f).toInt()
            )
            layout.addView(txtSin)
        }

        card.addView(layout)
        return card
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cálculos matemáticos
    // ─────────────────────────────────────────────────────────────────────────

    private fun calcularPromedio(curso: JSONObject): Float {
        val ep1 = toFloat(puntaje(curso, "ep1"))
        val parcial = toFloat(puntaje(curso, "parcial"))
        val ep2 = toFloat(puntaje(curso, "ep2"))
        val final = toFloat(puntaje(curso, "final"))
        return ep1 * 0.20f + parcial * 0.30f + ep2 * 0.20f + final * 0.30f
    }

    private fun calcularFinalNecesario(curso: JSONObject): String {
        val ep1Txt = puntaje(curso, "ep1")
        val parcialTxt = puntaje(curso, "parcial")
        val ep2Txt = puntaje(curso, "ep2")
        val finalTxt = puntaje(curso, "final")

        if (finalTxt.isNotBlank()) {
            val p = calcularPromedio(curso)
            return if (p >= 10.5f) "✅ Curso aprobado con ${"%.2f".format(p)}" else "🚨 Curso desaprobado · Promedio: ${"%.2f".format(p)}"
        }

        if (ep1Txt.isBlank() || parcialTxt.isBlank() || ep2Txt.isBlank()) {
            return "ℹ️ Completa EP1, Parcial y EP2 para calcular el final necesario"
        }

        val acumulado = toFloat(ep1Txt) * 0.20f + toFloat(parcialTxt) * 0.30f + toFloat(ep2Txt) * 0.20f
        val necesaria = (10.5f - acumulado) / 0.30f

        return when {
            necesaria <= 0f -> "✅ Ya estás aprobado antes del examen final"
            necesaria > 20f -> "🚨 No es posible aprobar (necesitarías ${"%.2f".format(necesaria)})"
            else -> "🎯 Necesitas ${"%.2f".format(necesaria)} en el examen final"
        }
    }

    private fun puntaje(curso: JSONObject, clave: String): String =
        curso.optJSONObject(clave)?.optString("puntaje", "") ?: ""

    private fun toFloat(valor: String): Float =
        valor.replace(",", ".").toFloatOrNull()?.coerceIn(0f, 20f) ?: 0f

    // ─────────────────────────────────────────────────────────────────────────
    //  Utilidades de UI
    // ─────────────────────────────────────────────────────────────────────────

    private fun crearTexto(
        texto: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(requireContext()).apply {
            text = texto
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun crearFondoColor(hexColor: String, radio: Float): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = radio
            setColor(Color.parseColor(hexColor))
        }
    }

    private fun colorHex(hex: String): Int = Color.parseColor(hex)

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density

    private fun formatearFecha(fecha: String): String {
        if (fecha.isBlank()) return "--"
        return try {
            val instant = java.time.Instant.parse(fecha)
            val zona = java.time.ZoneId.of("America/Lima")
            val local = instant.atZone(zona)
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(local)
        } catch (e: Exception) {
            fecha.replace("T", " ").replace("Z", "").take(16)
        }
    }

    private fun limpiarNombreSub(nombre: String): String =
        nombre
            .replace(Regex("^\\d+_SUB-"), "")
            .replace("SUBCOMPONENTE", "Sub")
            .trim()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}