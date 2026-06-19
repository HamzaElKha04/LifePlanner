package com.hamza.lifeplanner.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HabitRepository {

    // Instancia principal de Firebase Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Colección donde se guardan todos los hábitos.
    private val habitsCollection = db.collection("habits")

    // Crea un hábito nuevo en Firestore.
    fun addHabit(
        userId: String,
        title: String,
        subtitle: String,
        icon: String,
        colorHex: String,
        targetValue: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val newDoc = habitsCollection.document()

        val safeTarget = targetValue.coerceIn(1, 7)

        val habit = Habit(
            id = newDoc.id,
            title = title,
            subtitle = subtitle,
            icon = icon,
            colorHex = colorHex,
            currentValue = 0,
            targetValue = safeTarget,
            completed = false,
            completedDates = emptyList(),
            userId = userId
        )

        newDoc.set(habit)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear hábito")
            }
    }

    // Edita los datos principales de un hábito existente.
    fun updateHabit(
        habitId: String,
        title: String,
        subtitle: String,
        icon: String,
        colorHex: String,
        targetValue: Int,
        completedDates: List<String>,
        currentWeekDates: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val safeTarget = targetValue.coerceIn(1, 7)

        val currentWeekProgress = completedDates.count { date ->
            currentWeekDates.contains(date)
        }.coerceAtMost(safeTarget)

        val isCompletedThisWeek = currentWeekProgress >= safeTarget

        habitsCollection.document(habitId)
            .update(
                mapOf(
                    "title" to title.trim(),
                    "subtitle" to subtitle.trim(),
                    "icon" to icon.trim(),
                    "colorHex" to colorHex,
                    "targetValue" to safeTarget,
                    "currentValue" to currentWeekProgress,
                    "completed" to isCompletedThisWeek
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al editar hábito")
            }
    }

    // Escucha en tiempo real los hábitos del usuario actual.
    fun listenToHabits(
        userId: String,
        onDataChange: (List<Habit>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return habitsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onError(exception.message ?: "Error al cargar hábitos")
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Habit::class.java)
                } ?: emptyList()

                onDataChange(habits)
            }
    }

    // Marca o desmarca un hábito en una fecha concreta.
    fun toggleHabitDate(
        habitId: String,
        currentCompletedDates: List<String>,
        dateText: String,
        currentWeekDates: List<String>,
        targetValue: Int,
        onError: (String) -> Unit
    ) {
        val safeTarget = targetValue.coerceIn(1, 7)

        val updatedDates = if (currentCompletedDates.contains(dateText)) {
            currentCompletedDates.filter { it != dateText }
        } else {
            currentCompletedDates + dateText
        }.distinct()

        val currentWeekProgress = updatedDates.count { date ->
            currentWeekDates.contains(date)
        }.coerceAtMost(safeTarget)

        val isCompletedThisWeek = currentWeekProgress >= safeTarget

        habitsCollection.document(habitId)
            .update(
                mapOf(
                    "completedDates" to updatedDates,
                    "currentValue" to currentWeekProgress,
                    "targetValue" to safeTarget,
                    "completed" to isCompletedThisWeek
                )
            )
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al actualizar hábito")
            }
    }

    // Mantengo esta función por compatibilidad con versiones anteriores.
    fun updateHabitProgress(
        habitId: String,
        currentValue: Int,
        targetValue: Int,
        onError: (String) -> Unit
    ) {
        val safeTarget = targetValue.coerceIn(1, 7)
        val safeCurrent = currentValue.coerceIn(0, safeTarget)
        val isCompleted = safeCurrent >= safeTarget

        habitsCollection.document(habitId)
            .update(
                mapOf(
                    "currentValue" to safeCurrent,
                    "targetValue" to safeTarget,
                    "completed" to isCompleted
                )
            )
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al actualizar hábito")
            }
    }

    // Elimina un hábito de Firestore.
    fun deleteHabit(
        habitId: String,
        onError: (String) -> Unit
    ) {
        habitsCollection.document(habitId)
            .delete()
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al eliminar hábito")
            }
    }

    // Crea hábitos base preparados para funcionar por semana.
    fun createDefaultHabits(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val defaults = listOf(
            Habit(
                title = "Beber 2L de agua",
                subtitle = "Objetivo: 7 días por semana",
                icon = "💧",
                colorHex = "#2196F3",
                currentValue = 0,
                targetValue = 7,
                completed = false,
                completedDates = emptyList(),
                userId = userId
            ),
            Habit(
                title = "Ejercicio 30 min",
                subtitle = "Objetivo: 3 días por semana",
                icon = "🏃",
                colorHex = "#FF9800",
                currentValue = 0,
                targetValue = 3,
                completed = false,
                completedDates = emptyList(),
                userId = userId
            ),
            Habit(
                title = "Leer 20 minutos",
                subtitle = "Objetivo: 7 días por semana",
                icon = "📚",
                colorHex = "#7A3CFF",
                currentValue = 0,
                targetValue = 7,
                completed = false,
                completedDates = emptyList(),
                userId = userId
            ),
            Habit(
                title = "Meditar",
                subtitle = "Objetivo: 5 días por semana",
                icon = "🧘",
                colorHex = "#34C759",
                currentValue = 0,
                targetValue = 5,
                completed = false,
                completedDates = emptyList(),
                userId = userId
            ),
            Habit(
                title = "Dormir 8 horas",
                subtitle = "Objetivo: 7 días por semana",
                icon = "😴",
                colorHex = "#6A00F4",
                currentValue = 0,
                targetValue = 7,
                completed = false,
                completedDates = emptyList(),
                userId = userId
            )
        )

        val batch = db.batch()

        defaults.forEach { habit ->
            val doc = habitsCollection.document()
            batch.set(
                doc,
                habit.copy(id = doc.id)
            )
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear hábitos base")
            }
    }
}