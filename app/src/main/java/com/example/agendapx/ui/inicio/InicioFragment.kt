package com.example.agendapx.ui.inicio

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.agendapx.R
import com.example.agendapx.data.HorarioData
import com.example.agendapx.databinding.FragmentInicioBinding
import com.example.agendapx.ui.horario.HorarioFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        aplicarEstiloPremium()
        cargarResumen()
        activarClicks()
        activarCalculadoraAutomatica()
        calcularPromedioGeneral()
    }

    private fun aplicarEstiloPremium() {
        binding.txtPromedio.setTextColor(Color.parseColor("#0F172A"))
        binding.txtNotaNecesaria.setTextColor(Color.parseColor("#2563EB"))
        binding.txtEstado.setTextColor(Color.parseColor("#64748B"))
    }

    private fun cargarResumen() {
        val diaActual = obtenerDiaActual()
        val clasesHoy = HorarioData.obtenerCursosPorDia(diaActual)

        binding.txtSemana.text = "Semana ${calcularSemanaAcademica()}"

        binding.txtClasesHoy.text = if (clasesHoy.isEmpty()) {
            "Clases de hoy\nNo tienes clases programadas"
        } else {
            "Clases de hoy\n${clasesHoy.size} clases programadas"
        }
    }

    private fun activarClicks() {
        binding.txtClasesHoy.setOnClickListener {
            cambiarFragment(HorarioFragment())
        }

        binding.txtAbrirCalculadora.setOnClickListener {
            mostrarSolo("calculadora")
        }

        binding.btnVolverInicio.setOnClickListener {
            mostrarSolo("resumen")
        }
    }

    private fun mostrarSolo(seccion: String) {
        binding.seccionResumen.visibility = View.GONE
        binding.seccionCalculadora.visibility = View.GONE

        when (seccion) {
            "resumen" -> binding.seccionResumen.visibility = View.VISIBLE
            "calculadora" -> binding.seccionCalculadora.visibility = View.VISIBLE
        }
    }

    private fun cambiarFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.contenedorPrincipal, fragment)
            .commit()
    }

    private fun activarCalculadoraAutomatica() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calcularPromedioGeneral()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.edtEp1.addTextChangedListener(watcher)
        binding.edtParcial.addTextChangedListener(watcher)
        binding.edtEp2.addTextChangedListener(watcher)
        binding.edtFinal.addTextChangedListener(watcher)
    }

    private fun calcularPromedioGeneral() {
        val ep1 = obtenerNota(binding.edtEp1.text.toString())
        val parcial = obtenerNota(binding.edtParcial.text.toString())
        val ep2 = obtenerNota(binding.edtEp2.text.toString())
        val final = obtenerNota(binding.edtFinal.text.toString())

        val promedio = calcularPromedio(ep1, parcial, ep2, final)

        binding.txtPromedio.text = "Promedio actual: %.2f".format(promedio)

        binding.txtNotaNecesaria.text = calcularNotaNecesariaTexto(
            binding.edtEp1.text.toString(),
            binding.edtParcial.text.toString(),
            binding.edtEp2.text.toString()
        )

        aplicarEstado(binding.txtEstado, promedio)
    }

    private fun aplicarEstado(textView: TextView, promedio: Double) {
        when {
            promedio >= 10.5 -> {
                textView.text = "Estado: Aprobado ✅"
                textView.setTextColor(Color.parseColor("#16A34A"))
                textView.setTypeface(null, Typeface.BOLD)
            }

            promedio > 0 -> {
                textView.text = "Estado: En proceso ⚠️"
                textView.setTextColor(Color.parseColor("#F59E0B"))
                textView.setTypeface(null, Typeface.BOLD)
            }

            else -> {
                textView.text = "Ingresa tus notas para calcular automáticamente."
                textView.setTextColor(Color.parseColor("#64748B"))
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun calcularPromedio(ep1: Double, parcial: Double, ep2: Double, final: Double): Double {
        return (ep1 * 0.20) + (parcial * 0.30) + (ep2 * 0.20) + (final * 0.30)
    }

    private fun calcularNotaNecesariaTexto(ep1Texto: String, parcialTexto: String, ep2Texto: String): String {
        if (ep1Texto.isBlank() || parcialTexto.isBlank() || ep2Texto.isBlank()) {
            return "Final necesario: completa EP1, Parcial y EP2"
        }

        val ep1 = obtenerNota(ep1Texto)
        val parcial = obtenerNota(parcialTexto)
        val ep2 = obtenerNota(ep2Texto)

        val acumulado = (ep1 * 0.20) + (parcial * 0.30) + (ep2 * 0.20)
        val necesaria = (10.5 - acumulado) / 0.30

        return when {
            necesaria <= 0 -> "Final necesario: ya estás aprobado ✅"
            necesaria > 20 -> "Final necesario: necesitas más de 20 ⚠️"
            else -> "Final necesario: %.2f".format(necesaria)
        }
    }

    private fun obtenerNota(valor: String): Double {
        val nota = valor.toDoubleOrNull() ?: 0.0
        return nota.coerceIn(0.0, 20.0)
    }

    private fun calcularSemanaAcademica(): Int {
        val inicioSemana9 = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val hoy = Calendar.getInstance()
        val diferenciaMillis = hoy.timeInMillis - inicioSemana9.timeInMillis
        val semanasPasadas = (diferenciaMillis / (1000 * 60 * 60 * 24 * 7)).toInt()

        return 9 + semanasPasadas
    }

    private fun obtenerDiaActual(): String {
        val formato = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-ES"))
        val dia = formato.format(Date()).replaceFirstChar { it.uppercase() }

        return when (dia) {
            "Lunes" -> "Lunes"
            "Martes" -> "Martes"
            "Miércoles" -> "Miércoles"
            "Jueves" -> "Jueves"
            "Viernes" -> "Viernes"
            "Sábado" -> "Sábado"
            else -> "Lunes"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}