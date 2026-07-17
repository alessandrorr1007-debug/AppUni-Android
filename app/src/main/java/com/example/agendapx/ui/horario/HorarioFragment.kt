package com.example.agendapx.ui.horario

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agendapx.R
import com.example.agendapx.adapter.HorarioAdapter
import com.example.agendapx.data.HorarioData
import com.example.agendapx.databinding.FragmentHorarioBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HorarioFragment : Fragment() {

    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HorarioAdapter

    private val dias = listOf(
        "Lunes",
        "Martes",
        "Miércoles",
        "Jueves",
        "Viernes",
        "Sábado"
    )

    private var diaSeleccionado = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHorarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HorarioAdapter(emptyList())

        binding.rvCursos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCursos.adapter = adapter

        diaSeleccionado = obtenerDiaActual()

        crearBotonesDias()
        mostrarCursos(diaSeleccionado)
    }

    private fun crearBotonesDias() {
        binding.contenedorDias.removeAllViews()

        dias.forEach { dia ->
            val boton = Button(requireContext())

            boton.text = dia.take(3).uppercase()
            boton.textSize = 15f
            boton.isAllCaps = false
            boton.setPadding(26, 8, 26, 8)

            val params = LinearLayout.LayoutParams(190, 105)
            params.setMargins(0, 0, 18, 0)
            boton.layoutParams = params

            if (dia == diaSeleccionado) {
                boton.setTextColor(Color.WHITE)
                boton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_day_selected
                )
            } else {
                boton.setTextColor(Color.parseColor("#0F172A"))
                boton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_day_unselected
                )
            }

            boton.setOnClickListener {
                diaSeleccionado = dia
                crearBotonesDias()
                mostrarCursos(dia)
            }

            binding.contenedorDias.addView(boton)
        }
    }

    private fun mostrarCursos(dia: String) {
        binding.txtDiaSeleccionado.text = dia

        val cursosDelDia = HorarioData.obtenerCursosPorDia(dia)
        adapter.actualizarLista(cursosDelDia)

        binding.txtResumenDia.text = when (cursosDelDia.size) {
            0 -> "No tienes clases programadas"
            1 -> "Tienes 1 clase programada"
            else -> "Tienes ${cursosDelDia.size} clases programadas"
        }
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