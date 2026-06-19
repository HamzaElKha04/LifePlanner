package com.hamza.lifeplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Task
import com.hamza.lifeplanner.data.TaskRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Modelo interno para representar cada día de la fila semanal.
private data class CalendarDayItem(
    val label: String,
    val dayNumber: Int,
    val dateText: String,
    val hasTasks: Boolean,
    val allCompleted: Boolean
)

@Composable
fun CalendarScreen(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    onQuickAddClick: (String) -> Unit = {}
) {
    // Repositorio de tareas.
    val taskRepository = remember { TaskRepository() }

    // Usuario autenticado.
    val userId = authRepository.getCurrentUserId()

    // Lista de tareas cargadas desde Firebase.
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    // Fecha actual.
    val today = remember { Calendar.getInstance() }

    // Fecha seleccionada en el calendario.
    var selectedDateText by remember {
        mutableStateOf(calendarToDateTextCalendar(today))
    }

    // Evita que la selección inicial se repita al actualizar Firebase.
    var initialSelectionDone by remember { mutableStateOf(false) }

    // Escucha en tiempo real las tareas.
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

    // Solo usamos en calendario tareas que tengan fecha.
    val datedTasks = tasks
        .filter { it.dateText.isNotBlank() }
        .sortedWith { a, b -> compareTasksByDateCalendar(a, b) }

    // Selección inicial: hoy si tiene tareas, si no la primera tarea existente.
    LaunchedEffect(datedTasks) {
        if (!initialSelectionDone) {
            val todayText = calendarToDateTextCalendar(today)

            val hasTodayTasks = datedTasks.any {
                normalizeDateTextCalendar(it.dateText) == todayText
            }

            selectedDateText = when {
                hasTodayTasks -> todayText
                datedTasks.isNotEmpty() -> normalizeDateTextCalendar(datedTasks.first().dateText)
                else -> todayText
            }

            initialSelectionDone = true
        }
    }

    val selectedCalendar = dateTextToCalendarCalendar(selectedDateText)
        ?: Calendar.getInstance()

    val weekDays = buildWeekDaysFromSelectedDateCalendar(
        selectedDate = selectedCalendar,
        tasks = datedTasks
    )

    val tasksForSelectedDay = datedTasks
        .filter { normalizeDateTextCalendar(it.dateText) == normalizeDateTextCalendar(selectedDateText) }
        .sortedWith { a, b -> compareTasksForDayAgendaCalendar(a, b) }

    val completedToday = tasksForSelectedDay.count { it.completed }
    val pendingToday = tasksForSelectedDay.count { !it.completed }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CalendarHeader(
                selectedCalendar = selectedCalendar,
                onPreviousWeek = {
                    selectedDateText = moveSelectedDateByDaysCalendar(selectedCalendar, -7)
                },
                onNextWeek = {
                    selectedDateText = moveSelectedDateByDaysCalendar(selectedCalendar, 7)
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { item ->
                    CalendarDayChip(
                        item = item,
                        selected = normalizeDateTextCalendar(item.dateText) == normalizeDateTextCalendar(selectedDateText),
                        onClick = {
                            selectedDateText = item.dateText
                        }
                    )
                }
            }
        }

        item {
            SelectedDayHeader(
                selectedCalendar = selectedCalendar,
                pending = pendingToday,
                completed = completedToday,
                onQuickAddClick = {
                    onQuickAddClick(selectedDateText)
                }
            )
        }

        if (tasksForSelectedDay.isEmpty()) {
            item {
                EmptyCalendarDayCard(
                    selectedDateText = selectedDateText,
                    onQuickAddClick = {
                        onQuickAddClick(selectedDateText)
                    }
                )
            }
        } else {
            items(tasksForSelectedDay, key = { it.id }) { task ->
                PremiumCalendarTaskItem(
                    task = task,
                    timeText = if (task.timeText.isBlank()) "Sin hora" else task.timeText,
                    onCheckedChange = { isCompleted ->
                        taskRepository.updateTaskCompleted(
                            taskId = task.id,
                            completed = isCompleted,
                            onError = {}
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
private fun CalendarHeader(
    selectedCalendar: Calendar,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    // Cabecera con mes, año y navegación por semanas.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Semana anterior"
            )
        }

        Text(
            text = formatMonthYearCalendar(selectedCalendar),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextWeek) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Semana siguiente"
            )
        }
    }
}

@Composable
private fun CalendarDayChip(
    item: CalendarDayItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Chip de día semanal.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = item.label,
            color = Color(0xFF7A7A7A),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = if (selected) Color(0xFFFF2D95) else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = if (!selected && item.hasTasks) 1.dp else 0.dp,
                    color = if (item.allCompleted) Color(0xFF34C759) else Color(0xFFFF2D95),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.dayNumber.toString(),
                color = if (selected) Color.White else Color(0xFF222222),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = when {
                        !item.hasTasks -> Color.Transparent
                        item.allCompleted -> Color(0xFF34C759)
                        else -> Color(0xFFFF2D95)
                    },
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SelectedDayHeader(
    selectedCalendar: Calendar,
    pending: Int,
    completed: Int,
    onQuickAddClick: () -> Unit
) {
    // Cabecera del día seleccionado.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formatSelectedDayTitleCalendar(selectedCalendar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$pending pendientes · $completed completadas",
                color = Color(0xFF777777),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "+ Nueva tarea",
            color = Color(0xFFFF2D95),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onQuickAddClick() }
        )
    }
}

@Composable
private fun EmptyCalendarDayCard(
    selectedDateText: String,
    onQuickAddClick: () -> Unit
) {
    // Tarjeta mostrada cuando el día seleccionado no tiene tareas.
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
                text = "No tienes tareas para este día.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Puedes crear una tarea directamente para el $selectedDateText.",
                color = Color(0xFF777777)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "+ Crear tarea en este día",
                color = Color(0xFFFF2D95),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onQuickAddClick() }
            )
        }
    }
}

@Composable
private fun PremiumCalendarTaskItem(
    task: Task,
    timeText: String,
    onCheckedChange: (Boolean) -> Unit
) {
    // Colores según prioridad o si está completada.
    val accentColor = when {
        task.completed -> Color(0xFF34C759)
        task.priority == "Alta" -> Color(0xFFFF4D5A)
        task.priority == "Media" -> Color(0xFFFFC107)
        task.priority == "Baja" -> Color(0xFF34C759)
        else -> Color.Gray
    }

    val softColor = when {
        task.completed -> Color(0xFFEAF8EE)
        task.priority == "Alta" -> Color(0xFFFFEEF1)
        task.priority == "Media" -> Color(0xFFFFF8E7)
        task.priority == "Baja" -> Color(0xFFEAF8EE)
        else -> Color(0xFFF7F7F7)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timeText,
            modifier = Modifier
                .width(62.dp)
                .padding(top = 18.dp),
            color = Color(0xFF8A8A8A),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(
                containerColor = softColor
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
                        .width(4.dp)
                        .height(58.dp)
                        .background(accentColor, RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Checkbox(
                    checked = task.completed,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(8.dp))

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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = calendarSubtitleCalendar(task),
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = accentColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = calendarBadgeCalendar(task),
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Subtítulo de tarea en calendario.
private fun calendarSubtitleCalendar(task: Task): String {
    return if (task.completed) {
        "Tarea completada"
    } else {
        when (task.priority) {
            "Alta" -> "Alta prioridad"
            "Media" -> "Importante"
            "Baja" -> "Prioridad baja"
            else -> "Tarea planificada"
        }
    }
}

// Badge de tarea en calendario.
private fun calendarBadgeCalendar(task: Task): String {
    return if (task.completed) {
        "HECHA"
    } else {
        when (task.priority) {
            "Alta" -> "ALTA"
            "Media" -> "MEDIA"
            "Baja" -> "BAJA"
            else -> "TAREA"
        }
    }
}

// Construye la semana visible partiendo de lunes.
private fun buildWeekDaysFromSelectedDateCalendar(
    selectedDate: Calendar,
    tasks: List<Task>
): List<CalendarDayItem> {
    val mondayBased = selectedDate.clone() as Calendar
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

    val result = mutableListOf<CalendarDayItem>()

    repeat(7) { index ->
        val current = mondayBased.clone() as Calendar
        current.add(Calendar.DAY_OF_MONTH, index)

        val currentDateText = calendarToDateTextCalendar(current)

        val dayTasks = tasks.filter {
            normalizeDateTextCalendar(it.dateText) == currentDateText
        }

        result.add(
            CalendarDayItem(
                label = formatDayLabelCalendar(current),
                dayNumber = current.get(Calendar.DAY_OF_MONTH),
                dateText = currentDateText,
                hasTasks = dayTasks.isNotEmpty(),
                allCompleted = dayTasks.isNotEmpty() && dayTasks.all { it.completed }
            )
        )
    }

    return result
}

// Formato LUN, MAR, MIÉ...
private fun formatDayLabelCalendar(calendar: Calendar): String {
    val formatter = SimpleDateFormat("EEE", Locale("es", "ES"))
    return formatter.format(calendar.time)
        .replace(".", "")
        .uppercase(Locale("es", "ES"))
        .take(3)
}

// Formato mes año.
private fun formatMonthYearCalendar(calendar: Calendar): String {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val text = formatter.format(calendar.time)

    return text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
    }
}

// Título del día seleccionado.
private fun formatSelectedDayTitleCalendar(calendar: Calendar): String {
    val formatter = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
    val text = formatter.format(calendar.time)

    return text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
    }
}

// Mueve la fecha seleccionada por días.
private fun moveSelectedDateByDaysCalendar(calendar: Calendar, days: Int): String {
    val moved = calendar.clone() as Calendar
    moved.add(Calendar.DAY_OF_MONTH, days)
    return calendarToDateTextCalendar(moved)
}

// Orden general por fecha, hora y prioridad.
private fun compareTasksByDateCalendar(a: Task, b: Task): Int {
    val dateA = parseTaskDatePartsCalendar(a.dateText)
    val dateB = parseTaskDatePartsCalendar(b.dateText)

    if (dateA.year != dateB.year) return dateA.year.compareTo(dateB.year)
    if (dateA.month != dateB.month) return dateA.month.compareTo(dateB.month)
    if (dateA.day != dateB.day) return dateA.day.compareTo(dateB.day)

    val timeA = parseTimeToMinutesCalendar(a.timeText)
    val timeB = parseTimeToMinutesCalendar(b.timeText)

    if (timeA != timeB) return timeA.compareTo(timeB)

    return priorityValueCalendar(b.priority).compareTo(priorityValueCalendar(a.priority))
}

// Orden dentro del día seleccionado.
private fun compareTasksForDayAgendaCalendar(a: Task, b: Task): Int {
    val timeA = parseTimeToMinutesCalendar(a.timeText)
    val timeB = parseTimeToMinutesCalendar(b.timeText)

    if (timeA != timeB) return timeA.compareTo(timeB)

    if (a.completed != b.completed) {
        return if (!a.completed) -1 else 1
    }

    return priorityValueCalendar(b.priority).compareTo(priorityValueCalendar(a.priority))
}

// Partes de una fecha.
private data class TaskDatePartsCalendar(
    val year: Int,
    val month: Int,
    val day: Int
)

// Convierte fecha dd/MM/yyyy a partes numéricas.
private fun parseTaskDatePartsCalendar(dateText: String): TaskDatePartsCalendar {
    val parts = dateText.trim().split("/")

    if (parts.size != 3) {
        return TaskDatePartsCalendar(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }

    val day = parts[0].toIntOrNull() ?: Int.MAX_VALUE
    val month = parts[1].toIntOrNull() ?: Int.MAX_VALUE
    var year = parts[2].toIntOrNull() ?: Int.MAX_VALUE

    if (year != Int.MAX_VALUE && year < 100) {
        year += 2000
    }

    return TaskDatePartsCalendar(year, month, day)
}

// Convierte hora HH:mm a minutos.
private fun parseTimeToMinutesCalendar(timeText: String): Int {
    val parts = timeText.trim().split(":")

    if (parts.size != 2) return Int.MAX_VALUE

    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE
    val minute = parts[1].toIntOrNull() ?: return Int.MAX_VALUE

    return hour * 60 + minute
}

// Valor numérico de prioridad.
private fun priorityValueCalendar(priority: String): Int {
    return when (priority) {
        "Alta" -> 3
        "Media" -> 2
        "Baja" -> 1
        else -> 0
    }
}

// Normaliza fechas para comparar correctamente.
private fun normalizeDateTextCalendar(dateText: String): String {
    val calendar = dateTextToCalendarCalendar(dateText) ?: return dateText
    return calendarToDateTextCalendar(calendar)
}

// Convierte texto a Calendar.
private fun dateTextToCalendarCalendar(dateText: String): Calendar? {
    val parts = dateText.trim().split("/")

    if (parts.size != 3) return null

    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    var year = parts[2].toIntOrNull() ?: return null

    if (year < 100) year += 2000

    return buildCalendarCalendar(year, month, day)
}

// Crea Calendar limpio.
private fun buildCalendarCalendar(year: Int, month: Int, day: Int): Calendar {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

// Convierte Calendar a dd/MM/yyyy.
private fun calendarToDateTextCalendar(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}