package com.hamza.lifeplanner.data

// Modelo de datos del perfil del usuario.
// Se guarda en Firestore para recordar el nombre visible elegido por el usuario.
data class UserProfile(

    // Id del usuario autenticado en Firebase Auth.
    val userId: String = "",

    // Correo electrónico del usuario.
    val email: String = "",

    // Nombre que el usuario quiere mostrar dentro de la app.
    val displayName: String = ""
)