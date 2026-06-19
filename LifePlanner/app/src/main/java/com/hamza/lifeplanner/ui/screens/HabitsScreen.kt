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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Habit
import com.hamza.lifeplanner.data.HabitRepository
import java.util.Calendar

private enum class HabitFilter {
    ALL,
    ACTIVE,
    COMPLETED
}

private data class HabitWeekDay(
    val label: String,
    val dateText: String,
    val completed: Boolean
)

@Composable
fun HabitsScreen(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    val habitRepository = remember { HabitRepository() }
    val userId = authRepository.getCurrentUserId()

    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var selectedFilter by rememberSaveable { mutableStateOf(HabitFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    val today = remember { Calendar.getInstance() }
    val todayText = remember { calendarToDateTextHabit(today) }
    val currentWeekDates = remember { buildCurrentWeekDatesHabit(today) }

    DisposableEffect(userId) {
        var listener: ListenerRegistration? = null

        if (userId != null) {
            listener = habitRepository.listenToHabits(
                userId = userId,
                onDataChange = { habits = it },
                onError = { errorMessage = it }
            )
        }

        onDispose {
            listener?.remove()
        }
    }

    val sortedHabits = habits.sortedWith(
        compareBy<Habit> { habit ->
            isHabitCompletedThisWeekHabit(habit, currentWeekDates)
        }.thenBy { it.title.lowercase() }
    )

    val filteredHabits = when (selectedFilter) {
        HabitFilter.ALL -> sortedHabits
        HabitFilter.ACTIVE -> sortedHabits.filter {
            !isHabitCompletedThisWeekHabit(it, currentWeekDates)
        }
        HabitFilter.COMPLETED -> sortedHabits.filter {
            isHabitCompletedThisWeekHabit(it, currentWeekDates)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HabitsHeader(
                onAddClick = { showAddDialog = true }
            )
        }

        item {
            HabitsWeekInfoCard(
                habits = habits,
                todayText = todayText,
                currentWeekDates = currentWeekDates
            )
        }

        item {
            HabitsFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }

        if (habits.isEmpty()) {
            item {
                EmptyHabitsCard(
                    errorMessage = errorMessage,
                    onCreateDefaults = {
                        if (userId != null) {
                            habitRepository.createDefaultHabits(
                                userId = userId,
                                onSuccess = { errorMessage = "" },
                                onError = { errorMessage = it }
                            )
                        }
                    },
                    onAddClick = { showAddDialog = true }
                )
            }
        } else {
            if (filteredHabits.isEmpty()) {
                item {
                    EmptyFilteredHabitsCard(
                        selectedFilter = selectedFilter
                    )
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        todayText = todayText,
                        currentWeekDates = currentWeekDates,
                        onToggleToday = {
                            habitRepository.toggleHabitDate(
                                habitId = habit.id,
                                currentCompletedDates = habit.completedDates,
                                dateText = todayText,
                                currentWeekDates = currentWeekDates,
                                targetValue = habit.targetValue,
                                onError = { errorMessage = it }
                            )
                        },
                        onEdit = {
                            habitToEdit = habit
                        },
                        onDelete = {
                            habitToDelete = habit
                        }
                    )
                }
            }

            item {
                MotivationalHabitCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(170.dp))
        }
    }

    if (showAddDialog) {
        HabitFormDialog(
            dialogTitle = "Nuevo hábito",
            initialTitle = "",
            initialSubtitle = "",
            initialEmoji = "",
            initialTarget = "7",
            initialColor = "#FF2D95",
            onDismiss = { showAddDialog = false },
            onSave = { title, subtitle, icon, colorHex, target ->
                if (userId != null) {
                    habitRepository.addHabit(
                        userId = userId,
                        title = title,
                        subtitle = subtitle,
                        icon = icon,
                        colorHex = colorHex,
                        targetValue = target,
                        onSuccess = {
                            showAddDialog = false
                            errorMessage = ""
                        },
                        onError = { errorMessage = it }
                    )
                }
            }
        )
    }

    if (habitToEdit != null) {
        val selectedHabit = habitToEdit!!

        HabitFormDialog(
            dialogTitle = "Editar hábito",
            initialTitle = selectedHabit.title,
            initialSubtitle = selectedHabit.subtitle,
            initialEmoji = selectedHabit.icon,
            initialTarget = selectedHabit.targetValue.coerceIn(1, 7).toString(),
            initialColor = selectedHabit.colorHex,
            onDismiss = { habitToEdit = null },
            onSave = { title, subtitle, icon, colorHex, target ->
                habitRepository.updateHabit(
                    habitId = selectedHabit.id,
                    title = title,
                    subtitle = subtitle,
                    icon = icon,
                    colorHex = colorHex,
                    targetValue = target,
                    completedDates = selectedHabit.completedDates,
                    currentWeekDates = currentWeekDates,
                    onSuccess = {
                        habitToEdit = null
                        errorMessage = ""
                    },
                    onError = { errorMessage = it }
                )
            }
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Eliminar hábito",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que quieres eliminar \"${habitToDelete?.title}\"? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedHabit = habitToDelete

                        if (selectedHabit != null) {
                            habitRepository.deleteHabit(
                                habitId = selectedHabit.id,
                                onError = { errorMessage = it }
                            )
                        }

                        habitToDelete = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun HabitsHeader(
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Mis hábitos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Seguimiento semanal de tus rutinas.",
                color = Color(0xFF777777)
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = Color(0xFFFF2D95),
                    shape = CircleShape
                )
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Añadir hábito",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun HabitsWeekInfoCard(
    habits: List<Habit>,
    todayText: String,
    currentWeekDates: List<String>
) {
    val habitsDoneToday = habits.count { habit ->
        habit.completedDates.contains(todayText)
    }

    val completedWeeklyGoals = habits.count { habit ->
        isHabitCompletedThisWeekHabit(habit, currentWeekDates)
    }

    val description = if (habits.isEmpty()) {
        "Marca cada hábito cuando lo completes hoy. El progreso se calcula de lunes a domingo."
    } else {
        "Hoy llevas $habitsDoneToday/${habits.size} hábitos marcados. Esta semana has cumplido $completedWeeklyGoals objetivos."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F3FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Esta semana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color(0xFF666666),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HabitsFilterRow(
    selectedFilter: HabitFilter,
    onFilterSelected: (HabitFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == HabitFilter.ALL,
            onClick = { onFilterSelected(HabitFilter.ALL) },
            label = { Text("Todos") }
        )

        FilterChip(
            selected = selectedFilter == HabitFilter.ACTIVE,
            onClick = { onFilterSelected(HabitFilter.ACTIVE) },
            label = { Text("Activos") }
        )

        FilterChip(
            selected = selectedFilter == HabitFilter.COMPLETED,
            onClick = { onFilterSelected(HabitFilter.COMPLETED) },
            label = { Text("Completados") }
        )
    }
}

@Composable
private fun EmptyHabitsCard(
    errorMessage: String,
    onCreateDefaults: () -> Unit,
    onAddClick: () -> Unit
) {
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
                text = "Todavía no tienes hábitos.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Puedes crear tus propios hábitos o generar algunos hábitos base para empezar.",
                color = Color(0xFF666666)
            )

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreateDefaults,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2D95)
                )
            ) {
                Text("Crear hábitos base")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Crear hábito personalizado")
            }
        }
    }
}

@Composable
private fun EmptyFilteredHabitsCard(
    selectedFilter: HabitFilter
) {
    val title = when (selectedFilter) {
        HabitFilter.ACTIVE -> "No tienes hábitos activos."
        HabitFilter.COMPLETED -> "No tienes hábitos completados esta semana."
        HabitFilter.ALL -> "No tienes hábitos."
    }

    val subtitle = when (selectedFilter) {
        HabitFilter.ACTIVE -> "Todos tus hábitos han alcanzado su objetivo semanal o no tienes hábitos activos."
        HabitFilter.COMPLETED -> "Cuando un hábito llegue a su objetivo semanal, aparecerá aquí."
        HabitFilter.ALL -> "Crea un nuevo hábito para empezar."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7F7F7)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    todayText: String,
    currentWeekDates: List<String>,
    onToggleToday: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = habitAccentColor(habit.colorHex)
    val softColor = habitSoftColor(habit.colorHex)

    val safeTarget = habit.targetValue.coerceIn(1, 7)

    val weekProgress = habit.completedDates.count { date ->
        currentWeekDates.contains(date)
    }.coerceAtMost(safeTarget)

    val progress = (weekProgress.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    val completedThisWeek = weekProgress >= safeTarget
    val completedToday = habit.completedDates.contains(todayText)

    val weekDays = buildHabitWeekDays(
        currentWeekDates = currentWeekDates,
        completedDates = habit.completedDates
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completedThisWeek) Color(0xFFEAF8EE) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
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
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = habit.subtitle.ifBlank {
                            "Objetivo: $safeTarget días por semana"
                        },
                        color = Color(0xFF777777),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "$weekProgress/$safeTarget",
                    fontWeight = FontWeight.Bold,
                    color = if (completedThisWeek) Color(0xFF34C759) else Color(0xFF222222)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (completedThisWeek) Color(0xFF34C759) else accentColor,
                trackColor = accentColor.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HabitWeekRow(
                weekDays = weekDays,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitTodayButton(
                    completedToday = completedToday,
                    completedThisWeek = completedThisWeek,
                    accentColor = accentColor,
                    onClick = onToggleToday
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = habitStatusText(
                        completedToday = completedToday,
                        completedThisWeek = completedThisWeek
                    ),
                    color = habitStatusColor(
                        completedToday = completedToday,
                        completedThisWeek = completedThisWeek
                    ),
                    fontWeight = if (completedToday || completedThisWeek) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Editar hábito",
                    tint = Color(0xFF7A3CFF),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onEdit() }
                )

                Spacer(modifier = Modifier.size(14.dp))

                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Eliminar hábito",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
private fun HabitWeekRow(
    weekDays: List<HabitWeekDay>,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (item.completed) Color(0xFF34C759) else Color(0xFFF1F1F1),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.completed) "✓" else item.label,
                        color = if (item.completed) Color.White else Color(0xFF777777),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            color = if (item.completed) {
                                Color(0xFF34C759)
                            } else {
                                accentColor.copy(alpha = 0.25f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun HabitTodayButton(
    completedToday: Boolean,
    completedThisWeek: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val canClick = completedToday || !completedThisWeek

    val text = when {
        completedToday -> "Desmarcar hoy"
        completedThisWeek -> "Cumplido"
        else -> "Marcar hoy"
    }

    val backgroundColor = when {
        completedToday -> Color(0xFFEAF8EE)
        completedThisWeek -> Color(0xFFEAF8EE)
        else -> accentColor.copy(alpha = 0.14f)
    }

    val textColor = when {
        completedToday -> Color(0xFF34C759)
        completedThisWeek -> Color(0xFF34C759)
        else -> accentColor
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(enabled = canClick) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MotivationalHabitCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFF7A18),
                        Color(0xFFFF2D95),
                        Color(0xFF6A00F4)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(22.dp)
    ) {
        Column {
            Text(
                text = "“",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "La disciplina es el puente entre tus metas y tus logros.",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HabitFormDialog(
    dialogTitle: String,
    initialTitle: String,
    initialSubtitle: String,
    initialEmoji: String,
    initialTarget: String,
    initialColor: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        subtitle: String,
        icon: String,
        colorHex: String,
        target: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var subtitle by remember { mutableStateOf(initialSubtitle) }
    var emojiText by remember { mutableStateOf(initialEmoji) }
    var targetText by remember { mutableStateOf(initialTarget) }
    var selectedColor by remember { mutableStateOf(initialColor.ifBlank { "#FF2D95" }) }
    var errorMessage by remember { mutableStateOf("") }

    val quickEmojiOptions = listOf(
        "✅", "💧", "🏃", "📚", "🧘", "😴", "🏋️", "🍎", "🎮", "🧹", "🦷", "🚶"
    )

    val colorOptions = listOf(
        "#FF2D95",
        "#2196F3",
        "#FF9800",
        "#7A3CFF",
        "#34C759",
        "#6A00F4"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = dialogTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = ""
                    },
                    label = { Text("Nombre del hábito") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = {
                        subtitle = it
                        errorMessage = ""
                    },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Objetivo: 7 días por semana") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it.filter { char -> char.isDigit() }.take(1)
                        errorMessage = ""
                    },
                    label = { Text("Objetivo semanal") },
                    placeholder = { Text("Del 1 al 7") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Emoji",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = emojiText,
                    onValueChange = {
                        emojiText = it
                        errorMessage = ""
                    },
                    label = { Text("Escribe un emoji") },
                    placeholder = { Text("Ej: 💪") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickEmojiOptions.take(6).forEach { emoji ->
                        FilterChip(
                            selected = emojiText == emoji,
                            onClick = { emojiText = emoji },
                            label = { Text(emoji) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickEmojiOptions.drop(6).forEach { emoji ->
                        FilterChip(
                            selected = emojiText == emoji,
                            onClick = { emojiText = emoji },
                            label = { Text(emoji) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Color",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = habitAccentColor(colorHex),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        ) {
                            if (selectedColor == colorHex) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color.White, CircleShape)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val target = targetText.toIntOrNull() ?: 0

                    when {
                        title.isBlank() -> {
                            errorMessage = "Escribe un nombre"
                        }

                        subtitle.isBlank() -> {
                            errorMessage = "Escribe una descripción"
                        }

                        target !in 1..7 -> {
                            errorMessage = "El objetivo semanal debe estar entre 1 y 7"
                        }

                        emojiText.isBlank() -> {
                            errorMessage = "Elige o escribe un emoji"
                        }

                        else -> {
                            onSave(
                                title.trim(),
                                subtitle.trim(),
                                emojiText.trim(),
                                selectedColor,
                                target
                            )
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

// Texto corto para evitar que la tarjeta se vea cargada.
private fun habitStatusText(
    completedToday: Boolean,
    completedThisWeek: Boolean
): String {
    return when {
        completedThisWeek -> "Objetivo cumplido"
        completedToday -> "Hecho hoy"
        else -> "Pendiente hoy"
    }
}

// Color del estado del hábito.
private fun habitStatusColor(
    completedToday: Boolean,
    completedThisWeek: Boolean
): Color {
    return if (completedToday || completedThisWeek) {
        Color(0xFF34C759)
    } else {
        Color(0xFF777777)
    }
}

private fun isHabitCompletedThisWeekHabit(
    habit: Habit,
    currentWeekDates: List<String>
): Boolean {
    val safeTarget = habit.targetValue.coerceIn(1, 7)

    val weekProgress = habit.completedDates.count { date ->
        currentWeekDates.contains(date)
    }

    return weekProgress >= safeTarget
}

private fun buildCurrentWeekDatesHabit(today: Calendar): List<String> {
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
        result.add(calendarToDateTextHabit(current))
    }

    return result
}

private fun buildHabitWeekDays(
    currentWeekDates: List<String>,
    completedDates: List<String>
): List<HabitWeekDay> {
    val labels = listOf("L", "M", "X", "J", "V", "S", "D")

    return currentWeekDates.mapIndexed { index, dateText ->
        HabitWeekDay(
            label = labels.getOrElse(index) { "" },
            dateText = dateText,
            completed = completedDates.contains(dateText)
        )
    }
}

private fun calendarToDateTextHabit(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}

private fun habitAccentColor(colorHex: String): Color {
    return when (colorHex) {
        "#2196F3" -> Color(0xFF2196F3)
        "#FF9800" -> Color(0xFFFF9800)
        "#7A3CFF" -> Color(0xFF7A3CFF)
        "#34C759" -> Color(0xFF34C759)
        "#6A00F4" -> Color(0xFF6A00F4)
        else -> Color(0xFFFF2D95)
    }
}

private fun habitSoftColor(colorHex: String): Color {
    return when (colorHex) {
        "#2196F3" -> Color(0xFFEAF4FF)
        "#FF9800" -> Color(0xFFFFF3E0)
        "#7A3CFF" -> Color(0xFFF3EDFF)
        "#34C759" -> Color(0xFFEAF8EE)
        "#6A00F4" -> Color(0xFFF0E7FF)
        else -> Color(0xFFFFEEF3)
    }
}