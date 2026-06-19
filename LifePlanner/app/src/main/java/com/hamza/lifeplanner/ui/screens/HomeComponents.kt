package com.hamza.lifeplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hamza.lifeplanner.data.Habit
import com.hamza.lifeplanner.data.Task

@Composable
fun HomeHeroSection(
    greeting: String,
    displayName: String,
    todayTaskCount: Int,
    habitsDoneToday: Int,
    habitsTotalToday: Int,
    progressPercent: Int,
    onOpenAllTasks: () -> Unit,
    onOpenHabits: () -> Unit
) {
    // Evita mostrar 0/0 cuando todavía no hay hábitos.
    val habitsValue = if (habitsTotalToday == 0) {
        "0"
    } else {
        "$habitsDoneToday/$habitsTotalToday"
    }

    // Cabecera principal de Inicio con degradado.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFF2D95),
                        Color(0xFFFF4D5A),
                        Color(0xFFFF7A18)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$greeting, $displayName!",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Hoy puedes lograr mucho 💪",
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Avatar simple con inicial del usuario.
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.24f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Resumen rápido de tareas, hábitos y progreso.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeHeroStatCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenAllTasks() },
                    value = todayTaskCount.toString(),
                    label = "Tareas hoy"
                )

                HomeHeroStatCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenHabits() },
                    value = habitsValue,
                    label = "Hábitos hoy"
                )

                HomeHeroStatCard(
                    modifier = Modifier.weight(1f),
                    value = "$progressPercent%",
                    label = "Progreso"
                )
            }
        }
    }
}

@Composable
private fun HomeHeroStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    // Mini tarjeta del resumen superior.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TodayTasksSection(
    todayLabel: String,
    tasks: List<Task>,
    onQuickAddClick: () -> Unit,
    onOpenAllTasks: () -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit
) {
    // Sección de tareas de hoy.
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HOY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = todayLabel,
                color = Color(0xFF777777),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "No tienes tareas para hoy.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Puedes crear una nueva tarea o revisar todas tus tareas pendientes.",
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "+ Nueva tarea",
                            color = Color(0xFFFF2D95),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onQuickAddClick() }
                        )

                        Text(
                            text = "Ver todas",
                            color = Color(0xFF7A3CFF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenAllTasks() }
                        )
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tasks.take(3).forEach { task ->
                    HomeTaskItem(
                        task = task,
                        onCheckedChange = { checked ->
                            onTaskCheckedChange(task, checked)
                        }
                    )
                }

                if (tasks.size > 3) {
                    Text(
                        text = "Ver todas las tareas",
                        color = Color(0xFFFF2D95),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenAllTasks() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit
) {
    // Color según prioridad o estado completado.
    val accentColor = when {
        task.completed -> Color(0xFF34C759)
        task.priority == "Alta" -> Color(0xFFFF4D5A)
        task.priority == "Media" -> Color(0xFFFFA000)
        task.priority == "Baja" -> Color(0xFF34C759)
        else -> Color.Gray
    }

    val backgroundColor = when {
        task.completed -> Color(0xFFEAF8EE)
        task.priority == "Alta" -> Color(0xFFFFEEF1)
        task.priority == "Media" -> Color(0xFFFFF8E7)
        task.priority == "Baja" -> Color(0xFFEAF8EE)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 56.dp)
                    .background(accentColor, RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.size(10.dp))

            Checkbox(
                checked = task.completed,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.completed) Color(0xFF777777) else Color(0xFF222222),
                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = buildHomeTaskSubtitle(task),
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (task.completed) "HECHA" else task.priority.uppercase(),
                    color = accentColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TodayHabitsSection(
    habits: List<Habit>,
    todayText: String,
    currentWeekDates: List<String>,
    onOpenHabits: () -> Unit,
    onToggleHabitToday: (Habit) -> Unit
) {
    // Sección de hábitos de hoy en Inicio.
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hábitos de hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Ver hábitos",
                color = Color(0xFFFF2D95),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onOpenHabits() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (habits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Aún no tienes hábitos.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Crea tus hábitos para seguir tu progreso semanal.",
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Crear hábitos",
                        color = Color(0xFFFF2D95),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenHabits() }
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                habits.take(3).forEach { habit ->
                    HomeHabitMiniItem(
                        habit = habit,
                        todayText = todayText,
                        currentWeekDates = currentWeekDates,
                        onToggleToday = {
                            onToggleHabitToday(habit)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHabitMiniItem(
    habit: Habit,
    todayText: String,
    currentWeekDates: List<String>,
    onToggleToday: () -> Unit
) {
    // Tarjeta compacta de hábito en Inicio.
    val accentColor = habitAccentColorHome(habit.colorHex)
    val softColor = habitSoftColorHome(habit.colorHex)

    val safeTarget = habit.targetValue.coerceIn(1, 7)

    val weekProgress = habit.completedDates.count { date ->
        currentWeekDates.contains(date)
    }.coerceAtMost(safeTarget)

    val completedToday = habit.completedDates.contains(todayText)
    val completedThisWeek = weekProgress >= safeTarget

    // Evita mostrar "Marcar" si el objetivo semanal ya está cumplido.
    val canToggle = completedToday || !completedThisWeek

    val actionText = when {
        completedToday -> "Hecho"
        completedThisWeek -> "Cumplido"
        else -> "Marcar"
    }

    val actionBackground = if (completedToday || completedThisWeek) {
        Color(0xFFEAF8EE)
    } else {
        accentColor.copy(alpha = 0.14f)
    }

    val actionTextColor = if (completedToday || completedThisWeek) {
        Color(0xFF34C759)
    } else {
        accentColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completedThisWeek) Color(0xFFEAF8EE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = softColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.icon.ifBlank { "✅" },
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$weekProgress/$safeTarget esta semana",
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = actionBackground,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable(enabled = canToggle) { onToggleToday() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    color = actionTextColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Texto secundario de una tarea.
private fun buildHomeTaskSubtitle(task: Task): String {
    val time = if (task.timeText.isBlank()) {
        "Sin hora"
    } else {
        task.timeText
    }

    return "$time · Prioridad ${task.priority}"
}

// Color principal de hábito.
private fun habitAccentColorHome(colorHex: String): Color {
    return when (colorHex) {
        "#2196F3" -> Color(0xFF2196F3)
        "#FF9800" -> Color(0xFFFF9800)
        "#7A3CFF" -> Color(0xFF7A3CFF)
        "#34C759" -> Color(0xFF34C759)
        "#6A00F4" -> Color(0xFF6A00F4)
        else -> Color(0xFFFF2D95)
    }
}

// Fondo suave de hábito.
private fun habitSoftColorHome(colorHex: String): Color {
    return when (colorHex) {
        "#2196F3" -> Color(0xFFEAF4FF)
        "#FF9800" -> Color(0xFFFFF3E0)
        "#7A3CFF" -> Color(0xFFF3EDFF)
        "#34C759" -> Color(0xFFEAF8EE)
        "#6A00F4" -> Color(0xFFF0E7FF)
        else -> Color(0xFFFFEEF3)
    }
}