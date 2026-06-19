package com.hamza.lifeplanner.data

// Modelo de datos que representa un hábito dentro de LifePlanner.
// Cada hábito pertenece a un usuario concreto y se guarda en Firebase Firestore.
data class Habit(

    // Identificador único del documento en Firestore
    val id: String = "",

    // Nombre principal del hábito
    val title: String = "",

    // Texto descriptivo del hábito
    val subtitle: String = "",

    // Emoji usado como icono visual del hábito
    val icon: String = "✅",

    // Color principal del hábito guardado como texto hexadecimal
    val colorHex: String = "#FF2D95",

    // Progreso actual guardado para compatibilidad con versiones anteriores.
    // En la nueva versión semanal se calcula principalmente usando completedDates.
    val currentValue: Int = 0,

    // Objetivo semanal del hábito.
    // Ejemplo: 7 significa cumplirlo 7 días por semana.
    val targetValue: Int = 7,

    // Indica si el hábito está completado según el objetivo semanal.
    val completed: Boolean = false,

    // Lista de fechas en las que el usuario ha marcado el hábito como completado.
    // Formato usado: dd/MM/yyyy
    val completedDates: List<String> = emptyList(),

    // Usuario propietario del hábito
    val userId: String = ""
)