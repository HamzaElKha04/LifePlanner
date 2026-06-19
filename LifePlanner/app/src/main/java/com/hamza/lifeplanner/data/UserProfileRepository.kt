package com.hamza.lifeplanner.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserProfileRepository {

    // Instancia principal de Firebase Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Colección donde se guardan los perfiles de usuario.
    private val profilesCollection = db.collection("user_profiles")

    // Escucha en tiempo real el perfil del usuario.
    // Si cambia el nombre visible, la app lo recibe automáticamente.
    fun listenToProfile(
        userId: String,
        onDataChange: (UserProfile?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return profilesCollection.document(userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onError(exception.message ?: "Error al cargar perfil")
                    return@addSnapshotListener
                }

                val profile = snapshot?.toObject(UserProfile::class.java)
                onDataChange(profile)
            }
    }

    // Guarda o actualiza el nombre visible del usuario.
    // Uso set con merge para no borrar otros datos del perfil si existen.
    fun saveDisplayName(
        userId: String,
        email: String,
        displayName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val safeName = displayName.trim()

        val profileData = mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to safeName
        )

        profilesCollection.document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al guardar perfil")
            }
    }
}