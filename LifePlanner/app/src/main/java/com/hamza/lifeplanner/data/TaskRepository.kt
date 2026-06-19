package com.hamza.lifeplanner.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TaskRepository {

    // Instancia principal de Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Colección donde se guardan todas las tareas.
    private val tasksCollection = db.collection("tasks")

    // Crea una nueva tarea en Firestore.
    fun addTask(
        userId: String,
        title: String,
        priority: String,
        dateText: String,
        timeText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val newDoc = tasksCollection.document()

        val task = Task(
            id = newDoc.id,
            title = title,
            completed = false,
            userId = userId,
            priority = priority,
            dateText = dateText,
            timeText = timeText
        )

        newDoc.set(task)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al añadir tarea")
            }
    }

    // Escucha en tiempo real las tareas del usuario.
    // Cada vez que cambia algo en Firebase, la app recibe la lista actualizada.
    fun listenToTasks(
        userId: String,
        onDataChange: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return tasksCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onError(exception.message ?: "Error al cargar tareas")
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Task::class.java)
                } ?: emptyList()

                onDataChange(tasks)
            }
    }

    // Actualiza si una tarea está completada o pendiente.
    fun updateTaskCompleted(
        taskId: String,
        completed: Boolean,
        onError: (String) -> Unit
    ) {
        tasksCollection.document(taskId)
            .update("completed", completed)
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al actualizar tarea")
            }
    }

    // Edita los datos principales de una tarea ya existente.
    // Se usa desde la pantalla "Mis tareas".
    fun updateTask(
        taskId: String,
        title: String,
        priority: String,
        dateText: String,
        timeText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updatedData = mapOf(
            "title" to title.trim(),
            "priority" to priority,
            "dateText" to dateText,
            "timeText" to timeText
        )

        tasksCollection.document(taskId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al editar tarea")
            }
    }

    // Elimina una tarea de Firestore.
    fun deleteTask(
        taskId: String,
        onError: (String) -> Unit
    ) {
        tasksCollection.document(taskId)
            .delete()
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al eliminar tarea")
            }
    }
}