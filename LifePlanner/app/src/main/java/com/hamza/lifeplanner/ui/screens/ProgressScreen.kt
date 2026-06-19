package com.hamza.lifeplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Task
import com.hamza.lifeplanner.data.TaskRepository

@Composable
fun ProgressScreen(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    // Repositorio para leer las tareas guardadas en Firestore
    val taskRepository = remember { TaskRepository() }

    // Obtenemos el id del usuario actual
    val userId = authRepository.getCurrentUserId()

    // Estado local con la lista de tareas
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    // Escucha en tiempo real las tareas del usuario
    DisposableEffect(userId) {
        var listener: ListenerRegistration? = null

        if (userId != null) {
            listener = taskRepository.listenToTasks(
                userId = userId,
                onDataChange = { tasks = it },
                onError = {}
            )
        }

        onDispose {
            listener?.remove()
        }
    }

    // Cálculos generales para el progreso
    val total = tasks.size
    val completed = tasks.count { it.completed }
    val pending = tasks.count { !it.completed }

    // Cálculos por prioridad
    val high = tasks.count { it.priority == "Alta" }
    val medium = tasks.count { it.priority == "Media" }
    val low = tasks.count { it.priority == "Baja" }

    // Progreso general en porcentaje
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val progressPercent = (progress * 100).toInt()

    // Mensaje dinámico según el avance actual
    val progressMessage = when {
        total == 0 -> "Todavía no tienes tareas creadas. Empieza creando la primera."
        progressPercent < 40 -> "Has empezado. Lo importante ahora es mantener el ritmo."
        progressPercent < 80 -> "Vas muy bien. Ya has avanzado una buena parte."
        progressPercent < 100 -> "Estás muy cerca de completar todo lo pendiente."
        else -> "Perfecto. Has completado todas tus tareas."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título principal de la pantalla
        Text(
            text = "Progreso",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Subtítulo explicativo
        Text(
            text = "Resumen visual de tu actividad",
            color = Color(0xFF777777)
        )

        // Tarjeta principal con el porcentaje de avance
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F3FF)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Progreso general",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A3CFF)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = progressMessage,
                    color = Color(0xFF555555)
                )
            }
        }

        // Fila superior de métricas rápidas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProgressMiniCard(
                modifier = Modifier.weight(1f),
                title = "Totales",
                value = total.toString(),
                containerColor = Color(0xFFFFF2F5)
            )

            ProgressMiniCard(
                modifier = Modifier.weight(1f),
                title = "Pendientes",
                value = pending.toString(),
                containerColor = Color(0xFFFFF8E9)
            )

            ProgressMiniCard(
                modifier = Modifier.weight(1f),
                title = "Completadas",
                value = completed.toString(),
                containerColor = Color(0xFFEFFAF2)
            )
        }

        // Tarjeta con las prioridades
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF7F7F7)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Distribución por prioridad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                PriorityRow(
                    label = "Alta",
                    value = high,
                    color = Color(0xFFD50000)
                )

                Spacer(modifier = Modifier.height(10.dp))

                PriorityRow(
                    label = "Media",
                    value = medium,
                    color = Color(0xFFFFA000)
                )

                Spacer(modifier = Modifier.height(10.dp))

                PriorityRow(
                    label = "Baja",
                    value = low,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        // Tarjeta final de lectura rápida
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEEF7FF)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Estado actual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ahora mismo tienes $pending tareas pendientes y $completed completadas.",
                    color = Color(0xFF444444)
                )
            }
        }
    }
}

@Composable
private fun ProgressMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    containerColor: Color
) {
    // Tarjeta pequeña para mostrar métricas rápidas
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PriorityRow(
    label: String,
    value: Int,
    color: Color
) {
    // Fila individual para mostrar prioridades con punto de color
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value.toString(),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}