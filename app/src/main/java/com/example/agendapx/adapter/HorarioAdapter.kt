package com.example.agendapx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agendapx.data.Curso
import com.example.agendapx.databinding.ItemCursoBinding

class HorarioAdapter(
    private var cursos: List<Curso>
) : RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    inner class HorarioViewHolder(
        private val binding: ItemCursoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(curso: Curso) {
            binding.txtNombreCurso.text = curso.nombre
            binding.txtHoraCurso.text = curso.hora
            binding.txtAulaCurso.text = "📍 Aula: ${curso.aula}"
            binding.txtNrcCurso.text = "🔢 NRC: ${curso.nrc}"

            binding.txtIconoCurso.text = when {
                curso.nombre.contains("Aplicaciones", true) -> "📱"
                curso.nombre.contains("Infraestructura", true) -> "☁️"
                curso.nombre.contains("Inteligencia", true) -> "🤖"
                curso.nombre.contains("Agile", true) -> "📋"
                curso.nombre.contains("Deontología", true) -> "⚖️"
                curso.nombre.contains("Metodología", true) -> "📖"
                else -> "📚"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val binding = ItemCursoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        holder.bind(cursos[position])
    }

    override fun getItemCount(): Int = cursos.size

    fun actualizarLista(nuevaLista: List<Curso>) {
        cursos = nuevaLista
        notifyDataSetChanged()
    }
}