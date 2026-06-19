package com.hamza.lifeplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Habit
import com.hamza.lifeplanner.data.HabitRepository
import com.hamza.lifeplanner.data.Task
import com.hamza.lifeplanner.data.TaskRepository
import com.hamza.lifeplanner.data.UserProfileRepository
import java.util.Calendar
import java.util.Locale

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Repositorios usados por la pantalla.
    val taskRepository = remember { TaskRepository() }
    val habitRepository = remember { HabitRepository() }
    val userProfileRepository = remember { UserProfileRepository() }

    // Datos del usuario autenticado.
    val userId = authRepository.getCurrentUserId()
    val email = authRepository.getCurrentUserEmail()

    // Estados cargados desde Firebase.
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var displayName by remember { mutableStateOf("") }

    // Control de diálogos.
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Mensaje de error general.
    var errorMessage by remember { mutableStateOf("") }

    // Semana actual para calcular progreso semanal de hábitos.
    val today = remember { Calendar.getInstance() }
    val currentWeekDates = remember { buildCurrentWeekDatesProfile(today) }

    // Escuchamos tareas, hábitos y perfil en tiempo real.
    DisposableEffect(userId) {
        var taskListener: ListenerRegistration? = null
        var habitListener: ListenerRegistration? = null
        var profileListener: ListenerRegistration? = null

        if (userId != null) {
            taskListener = taskRepository.listenToTasks(
                userId = userId,
                onDataChange = { tasks = it },
                onError = { errorMessage = it }
            )

            habitListener = habitRepository.listenToHabits(
                userId = userId,
                onDataChange = { habits = it },
                onError = { errorMessage = it }
            )

            profileListener = userProfileRepository.listenToProfile(
                userId = userId,
                onDataChange = { profile ->
                    displayName = profile?.displayName.orEmpty()
                },
                onError = { errorMessage = it }
            )
        }

        onDispose {
            taskListener?.remove()
            habitListener?.remove()
            profileListener?.remove()
        }
    }

    // Nombre que se muestra en pantalla.
    // Si no hay nombre guardado, se genera uno básico desde el email.
    val visibleName = if (displayName.isNotBlank()) {
        displayName
    } else {
        displayNameFromEmailProfile(email)
    }

    // Estadísticas de tareas.
    val totalTasks = tasks.size
    val pendingTasks = tasks.count { !it.completed }
    val completedTasks = tasks.count { it.completed }

    // Estadísticas de hábitos.
    val totalHabits = habits.size
    val completedWeeklyHabits = habits.count { habit ->
        isHabitCompletedThisWeekProfile(habit, currentWeekDates)
    }

    val habitProgressPercent = calculateWeeklyHabitsProgressProfile(
        habits = habits,
        currentWeekDates = currentWeekDates
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ProfileHeaderCard(
                visibleName = visibleName,
                email = email,
                onEditClick = {
                    showEditNameDialog = true
                }
            )
        }

        if (errorMessage.isNotBlank()) {
            item {
                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        item {
            AccountInfoCard()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    value = totalTasks.toString(),
                    label = "Tareas",
                    backgroundColor = Color(0xFFFFF2F5)
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    value = pendingTasks.toString(),
                    label = "Pendientes",
                    backgroundColor = Color(0xFFFFF8E9)
                )

                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    value = completedTasks.toString(),
                    label = "Hechas",
                    backgroundColor = Color(0xFFEAF8EE)
                )
            }
        }

        item {
            HabitsProfileCard(
                totalHabits = totalHabits,
                completedWeeklyHabits = completedWeeklyHabits,
                progressPercent = habitProgressPercent
            )
        }

        item {
            MotivationProfileCard()
        }

        item {
            Button(
                onClick = {
                    showLogoutDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("Cerrar sesión")
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Diálogo para editar nombre visible.
    if (showEditNameDialog) {
        EditProfileNameDialog(
            initialName = visibleName,
            onDismiss = {
                showEditNameDialog = false
            },
            onSave = { newName ->
                when {
                    userId == null -> {
                        errorMessage = "No se ha encontrado el usuario"
                    }

                    newName.trim().length < 2 -> {
                        errorMessage = "El nombre debe tener al menos 2 caracteres"
                    }

                    newName.trim().length > 20 -> {
                        errorMessage = "El nombre no puede superar 20 caracteres"
                    }

                    else -> {
                        userProfileRepository.saveDisplayName(
                            userId = userId,
                            email = email,
                            displayName = newName.trim(),
                            onSuccess = {
                                errorMessage = ""
                                showEditNameDialog = false
                            },
                            onError = { errorMessage = it }
                        )
                    }
                }
            }
        )
    }

    // Confirmación antes de cerrar sesión.
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que quieres cerrar sesión? Tus datos seguirán guardados en Firebase."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authRepository.logout()
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    visibleName: String,
    email: String,
    onEditClick: () -> Unit
) {
    // Tarjeta superior con avatar, nombre, correo y botón editar.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F1FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFFF2D95),
                                Color(0xFF7A3CFF)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = visibleName.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = visibleName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Editar perfil",
                tint = Color(0xFF7A3CFF),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onEditClick() }
            )
        }
    }
}

@Composable
private fun AccountInfoCard() {
    // Información general de la cuenta y sincronización.
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
                text = "Tu cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Aplicación: LifePlanner")
            Text("Estado: sesión iniciada")
            Text("Sincronización: Firebase activa")
        }
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    backgroundColor: Color
) {
    // Tarjeta pequeña de estadísticas.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
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
                text = label,
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HabitsProfileCard(
    totalHabits: Int,
    completedWeeklyHabits: Int,
    progressPercent: Int
) {
    // Tarjeta de resumen de hábitos semanales.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F3FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Hábitos semanales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF7A3CFF),
                trackColor = Color(0xFFE6D8FF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Progreso semanal: $progressPercent%")
            Text("Hábitos creados: $totalHabits")
            Text("Objetivos cumplidos: $completedWeeklyHabits")
        }
    }
}

@Composable
private fun MotivationProfileCard() {
    // Tarjeta motivadora simple.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF4FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Frase motivadora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Organiza tu vida. Cumple tus metas.")
        }
    }
}

@Composable
private fun EditProfileNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // Estado local del nombre mientras se edita.
    var name by remember { mutableStateOf(initialName) }
    var localError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Editar nombre",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = ""
                    },
                    label = { Text("Nombre visible") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                if (localError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = localError,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.trim().length < 2 -> {
                            localError = "El nombre debe tener al menos 2 caracteres"
                        }

                        name.trim().length > 20 -> {
                            localError = "El nombre no puede superar 20 caracteres"
                        }

                        else -> {
                            onSave(name.trim())
                        }
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Genera un nombre visible básico desde el correo si el usuario todavía no ha guardado uno.
private fun displayNameFromEmailProfile(email: String): String {
    val base = email.substringBefore("@")
        .substringBefore(".")
        .substringBefore("_")
        .substringBefore("-")
        .trim()

    if (base.isBlank()) return "Usuario"

    return base.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

// Construye la semana actual de lunes a domingo.
private fun buildCurrentWeekDatesProfile(today: Calendar): List<String> {
    val mondayBased = today.clone() as Calendar
    val currentDayOfWeek = mondayBased.get(Calendar.DAY_OF_WEEK)

    val diff = when (currentDayOfWeek) {
        Calendar.SUNDAY -> -6
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> -1
        Calendar.WEDNESDAY -> -2
        Calendar.THURSDAY -> -3
        Calendar.FRIDAY -> -4
        Calendar.SATURDAY -> -5
        else -> 0
    }

    mondayBased.add(Calendar.DAY_OF_MONTH, diff)

    val result = mutableListOf<String>()

    repeat(7) { index ->
        val current = mondayBased.clone() as Calendar
        current.add(Calendar.DAY_OF_MONTH, index)
        result.add(calendarToDateTextProfile(current))
    }

    return result
}

// Convierte Calendar a formato dd/MM/yyyy.
private fun calendarToDateTextProfile(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}

// Comprueba si un hábito ha alcanzado su objetivo semanal.
private fun isHabitCompletedThisWeekProfile(
    habit: Habit,
    currentWeekDates: List<String>
): Boolean {
    val safeTarget = habit.targetValue.coerceIn(1, 7)

    val weekProgress = habit.completedDates.count { date ->
        currentWeekDates.contains(date)
    }

    return weekProgress >= safeTarget
}

// Calcula el porcentaje semanal de hábitos.
private fun calculateWeeklyHabitsProgressProfile(
    habits: List<Habit>,
    currentWeekDates: List<String>
): Int {
    if (habits.isEmpty()) return 0

    val totalTarget = habits.sumOf { habit ->
        habit.targetValue.coerceIn(1, 7)
    }

    if (totalTarget == 0) return 0

    val completedThisWeek = habits.sumOf { habit ->
        val safeTarget = habit.targetValue.coerceIn(1, 7)

        habit.completedDates.count { date ->
            currentWeekDates.contains(date)
        }.coerceAtMost(safeTarget)
    }

    return ((completedThisWeek.toFloat() / totalTarget.toFloat()) * 100).toInt()
}