package com.example.agendapx.data

object HorarioData {

    val cursos = listOf(
        Curso("Aplicaciones Móviles para los Negocios", "Lunes", "08:50 - 12:25", "Ninguno", "5636"),
        Curso("Infraestructura como Código", "Lunes", "19:50 - 21:35", "C405", "5592"),

        Curso("Deontología Profesional", "Martes", "08:50 - 09:40", "Ninguno", "6645"),
        Curso("Deontología Profesional", "Martes", "09:45 - 11:30", "Ninguno", "6646"),
        Curso("Agile Development", "Martes", "19:50 - 21:35", "G606", "5585"),

        Curso("Infraestructura como Código", "Miércoles", "07:00 - 10:35", "G701", "8440"),
        Curso("Inteligencia Artificial", "Miércoles", "16:10 - 17:55", "G703", "5598"),
        Curso("Inteligencia Artificial", "Miércoles", "18:00 - 19:45", "G703", "5599"),

        Curso("Inteligencia Artificial", "Jueves", "16:10 - 17:55", "G701", "6493"),

        Curso("Aplicaciones Móviles para Negocios", "Viernes", "08:50 - 12:25", "Ninguno", "5637"),
        Curso("Agile Development", "Viernes", "18:00 - 21:35", "G201", "5587"),

        Curso("Metodología de la Investigación Científica", "Sábado", "14:20 - 16:05", "Ninguno", "3233"),
        Curso("Metodología de la Investigación Científica", "Sábado", "16:10 - 17:55", "Ninguno", "3234")
    )

    fun obtenerCursosPorDia(dia: String): List<Curso> {
        return cursos.filter { it.dia == dia }
    }
}