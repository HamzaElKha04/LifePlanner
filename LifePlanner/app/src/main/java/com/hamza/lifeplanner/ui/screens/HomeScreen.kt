package com.hamza.lifeplanner.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Habit
import com.hamza.lifeplanner.data.HabitRepository
import com.hamza.lifeplanner.data.Task
import com.hamza.lifeplanner.data.TaskRepository
import com.hamza.lifeplanner.data.UserProfileRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onQuickAddClick: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAllTasks: () -> Unit = {},
    onOpenPendingTasks: () -> Unit = {},
    onOpenCompletedTasks: () -> Unit = {}
) {
    // Repositorio encargado de leer y modificar tareas en Firebase.
    val taskRepository = remember { TaskRepository() }

    // Repositorio encargado de leer y modificar hábitos en Firebase.
    val habitRepository = remember { HabitRepository() }

    // Repositorio encargado de leer el perfil del usuario.
    val userProfileRepository = remember { UserProfileRepository() }

    // Datos del usuario autenticado.
    val userEmail = authRepository.getCurrentUserEmail()
    val userId = authRepository.getCurrentUserId()

    // Listas cargadas desde Firebase.
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }

    // Nombre visible guardado en el perfil.
    var profileDisplayName by remember { mutableStateOf("") }

    // Fecha actual del dispositivo.
    val today = remember { Calendar.getInstance() }

    // Fecha de hoy en formato dd/MM/yyyy.
    val todayText = remember { calendarToDateTextHome(today) }

    // Semana actual de lunes a domingo.
    val currentWeekDates = remember { buildCurrentWeekDatesHome(today) }

    // Escucha en tiempo real tareas, hábitos y perfil del usuario.
    DisposableEffect(userId) {
        var taskListener: ListenerRegistration? = null
        var habitListener: ListenerRegistration? = null
        var profileListener: ListenerRegistration? = null

        if (userId != null) {
            taskListener = taskRepository.listenToTasks(
                userId = userId,
                onDataChange = { tasks = it },
                onError = {}
            )

            habitListener = habitRepository.listenToHabits(
                userId = userId,
                onDataChange = { habits = it },
                onError = {}
            )

            profileListener = userProfileRepository.listenToProfile(
                userId = userId,
                onDataChange = { profile ->
                    profileDisplayName = profile?.displayName.orEmpty()
                },
                onError = {}
            )
        }

        onDispose {
            taskListener?.remove()
            habitListener?.remove()
            profileListener?.remove()
        }
    }

    // Tareas ordenadas por pendientes, fecha, hora y prioridad.
    val sortedTasks = tasks.sortedWith { a, b ->
        compareTasksHome(a, b)
    }

    // Tareas del día actual.
    val todayTasks = sortedTasks.filter { task ->
        normalizeDateTextHome(task.dateText) == todayText
    }

    // Hábitos ordenados mostrando primero los que no han cumplido objetivo semanal.
    val sortedHabits = habits.sortedWith(
        compareBy<Habit> { habit ->
            isHabitCompletedThisWeekHome(habit, currentWeekDates)
        }.thenBy { it.title.lowercase() }
    )

    // Estadísticas de tareas.
    val todayTaskCount = todayTasks.size
    val completedTaskCount = tasks.count { it.completed }
    val totalTaskCount = tasks.size

    // Hábitos que cuentan para hoy.
    // Si un hábito ya cumplió su objetivo semanal y no está marcado hoy,
    // no se cuenta como pendiente de hoy.
    val habitsForTodayCounter = habits.filter { habit ->
        val completedToday = habit.completedDates.contains(todayText)
        val completedThisWeek = isHabitCompletedThisWeekHome(habit, currentWeekDates)

        completedToday || !completedThisWeek
    }

    val habitsDoneToday = habitsForTodayCounter.count { habit ->
        habit.completedDates.contains(todayText)
    }

    val habitsTotalToday = habitsForTodayCounter.size

    // Progreso semanal de hábitos.
    val weeklyProgressPercent = calculateWeeklyHabitsProgressHome(
        habits = habits,
        currentWeekDates = currentWeekDates
    )

    // Si todavía no hay hábitos, usamos el progreso de tareas como respaldo.
    val progressPercent = if (habits.isNotEmpty()) {
        weeklyProgressPercent
    } else {
        if (totalTaskCount == 0) {
            0
        } else {
            ((completedTaskCount.toFloat() / totalTaskCount.toFloat()) * 100).toInt()
        }
    }

    // Nombre real que viene de Firebase o, si no existe, desde el correo.
    val fullDisplayName = if (profileDisplayName.isNotBlank()) {
        profileDisplayName
    } else {
        displayNameFromEmailHome(userEmail)
    }

    // En Inicio usamos una versión corta para que la cabecera no se rompa.
    // Ejemplo: "Hamza TFG" en Perfil, pero "Hamza" en Inicio.
    val displayName = compactDisplayNameForHome(fullDisplayName)

    // Saludo según la hora del día.
    val greeting = getGreetingHome(today)

    // Fecha legible para la sección HOY.
    val todayLabel = formatTodayLabelHome(today)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            HomeHeroSection(
                greeting = greeting,
                displayName = displayName,
                todayTaskCount = todayTaskCount,
                habitsDoneToday = habitsDoneToday,
                habitsTotalToday = habitsTotalToday,
                progressPercent = progressPercent,
                onOpenAllTasks = onOpenAllTasks,
                onOpenHabits = onOpenProgress
            )

            Spacer(modifier = Modifier.height(22.dp))

            TodayTasksSection(
                todayLabel = todayLabel,
                tasks = todayTasks,
                onQuickAddClick = onQuickAddClick,
                onOpenAllTasks = onOpenAllTasks,
                onTaskCheckedChange = { task, checked ->
                    taskRepository.updateTaskCompleted(
                        taskId = task.id,
                        completed = checked,
                        onError = {}
                    )
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            TodayHabitsSection(
                habits = sortedHabits,
                todayText = todayText,
                currentWeekDates = currentWeekDates,
                onOpenHabits = onOpenProgress,
                onToggleHabitToday = { habit ->
                    habitRepository.toggleHabitDate(
                        habitId = habit.id,
                        currentCompletedDates = habit.completedDates,
                        dateText = todayText,
                        currentWeekDates = currentWeekDates,
                        targetValue = habit.targetValue,
                        onError = {}
                    )
                }
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// Ordena tareas por estado, fecha, hora y prioridad.
private fun compareTasksHome(a: Task, b: Task): Int {
    if (a.completed != b.completed) {
        return if (!a.completed) -1 else 1
    }

    val dateA = parseTaskDatePartsHome(a.dateText)
    val dateB = parseTaskDatePartsHome(b.dateText)

    if (dateA.year != dateB.year) return dateA.year.compareTo(dateB.year)
    if (dateA.month != dateB.month) return dateA.month.compareTo(dateB.month)
    if (dateA.day != dateB.day) return dateA.day.compareTo(dateB.day)

    val timeA = parseTimeToMinutesHome(a.timeText)
    val timeB = parseTimeToMinutesHome(b.timeText)

    if (timeA != timeB) return timeA.compareTo(timeB)

    return priorityValueHome(b.priority).compareTo(priorityValueHome(a.priority))
}

// Clase auxiliar para comparar fechas.
private data class TaskDatePartsHome(
    val year: Int,
    val month: Int,
    val day: Int
)

// Convierte una fecha de texto a partes numéricas.
private fun parseTaskDatePartsHome(dateText: String): TaskDatePartsHome {
    val parts = dateText.trim().split("/")

    if (parts.size != 3) {
        return TaskDatePartsHome(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }

    val day = parts[0].toIntOrNull() ?: Int.MAX_VALUE
    val month = parts[1].toIntOrNull() ?: Int.MAX_VALUE
    var year = parts[2].toIntOrNull() ?: Int.MAX_VALUE

    if (year != Int.MAX_VALUE && year < 100) {
        year += 2000
    }

    return TaskDatePartsHome(year, month, day)
}

// Convierte hora HH:mm a minutos para ordenar.
private fun parseTimeToMinutesHome(timeText: String): Int {
    val parts = timeText.trim().split(":")

    if (parts.size != 2) return Int.MAX_VALUE

    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE
    val minute = parts[1].toIntOrNull() ?: return Int.MAX_VALUE

    return hour * 60 + minute
}

// Valor numérico de prioridad.
private fun priorityValueHome(priority: String): Int {
    return when (priority) {
        "Alta" -> 3
        "Media" -> 2
        "Baja" -> 1
        else -> 0
    }
}

// Convierte Calendar a formato dd/MM/yyyy.
private fun calendarToDateTextHome(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}

// Normaliza fechas para comparar formatos tipo 4/5/26 y 04/05/2026.
private fun normalizeDateTextHome(dateText: String): String {
    val calendar = dateTextToCalendarHome(dateText) ?: return dateText
    return calendarToDateTextHome(calendar)
}

// Convierte texto dd/MM/yyyy a Calendar.
private fun dateTextToCalendarHome(dateText: String): Calendar? {
    val parts = dateText.trim().split("/")

    if (parts.size != 3) return null

    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    var year = parts[2].toIntOrNull() ?: return null

    if (year < 100) year += 2000

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

// Devuelve el saludo según la hora.
private fun getGreetingHome(calendar: Calendar): String {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)

    return when {
        hour < 12 -> "¡Buenos días"
        hour < 20 -> "¡Buenas tardes"
        else -> "¡Buenas noches"
    }
}

// Formatea la fecha de hoy en español.
private fun formatTodayLabelHome(calendar: Calendar): String {
    val formatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val text = formatter.format(calendar.time)

    return text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
    }
}

// Genera un nombre básico desde el correo si no hay nombre guardado.
private fun displayNameFromEmailHome(email: String): String {
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

// Devuelve una versión corta del nombre para usar en la cabecera de Inicio.
private fun compactDisplayNameForHome(name: String): String {
    val cleanName = name.trim()

    if (cleanName.isBlank()) return "Usuario"

    val firstPart = cleanName.split(" ")
        .firstOrNull()
        ?.trim()
        .orEmpty()

    return if (firstPart.isBlank()) {
        cleanName
    } else {
        firstPart
    }
}

// Construye la semana actual de lunes a domingo.
private fun buildCurrentWeekDatesHome(today: Calendar): List<String> {
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
        result.add(calendarToDateTextHome(current))
    }

    return result
}

// Comprueba si un hábito ha alcanzado su objetivo semanal.
private fun isHabitCompletedThisWeekHome(
    habit: Habit,
    currentWeekDates: List<String>
): Boolean {
    val safeTarget = habit.targetValue.coerceIn(1, 7)

    val weekProgress = habit.completedDates.count { date ->
        currentWeekDates.contains(date)
    }

    return weekProgress >= safeTarget
}

// Calcula el porcentaje general semanal de hábitos.
private fun calculateWeeklyHabitsProgressHome(
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