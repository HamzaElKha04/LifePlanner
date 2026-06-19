package com.hamza.lifeplanner.data

// Esta data class representa el modelo de una tarea dentro de la aplicación.
// Aquí se define qué información tiene cada tarea.
data class Task(

    // Identificador único de la tarea en Firestore
    val id: String = "",

    // Título o nombre de la tarea
    val title: String = "",

    // Indica si la tarea está completada o no
    val completed: Boolean = false,

    // ID del usuario al que pertenece la tarea
    val userId: String = "",

    // Prioridad de la tarea: Baja, Media o Alta
    val priority: String = "Media",

    // Fecha de la tarea en formato texto
    val dateText: String = "",

    // Hora de la tarea en formato texto
    val timeText: String = ""
)