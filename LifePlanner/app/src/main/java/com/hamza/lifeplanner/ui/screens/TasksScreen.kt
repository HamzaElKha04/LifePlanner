package com.hamza.lifeplanner.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.Task
import com.hamza.lifeplanner.data.TaskRepository
import java.util.Calendar

// Filtros disponibles en la pantalla "Mis tareas".
// No es private porque MainTabsScreen lo usa para abrir la pantalla con un filtro concreto.
enum class TaskFilterType {
    ALL,
    PENDING,
    COMPLETED
}

@Composable
fun TasksScreen(
    authRepository: AuthRepository,
    initialFilter: TaskFilterType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Repositorio de tareas conectado a Firestore.
    val taskRepository = remember { TaskRepository() }

    // Usuario autenticado.
    val userId = authRepository.getCurrentUserId()

    // Tareas cargadas desde Firebase.
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    // Filtro seleccionado. Se reinicia correctamente según el filtro inicial recibido.
    var selectedFilter by rememberSaveable(initialFilter) { mutableStateOf(initialFilter) }

    // Tarea seleccionada para eliminar.
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // Tarea seleccionada para editar.
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Mensaje de error general.
    var errorMessage by remember { mutableStateOf("") }

    // Escucha en tiempo real las tareas del usuario.
    DisposableEffect(userId) {
        var listener: ListenerRegistration? = null

        if (userId != null) {
            listener = taskRepository.listenToTasks(
                userId = userId,
                onDataChange = { tasks = it },
                onError = { errorMessage = it }
            )
        }

        onDispose {
            listener?.remove()
        }
    }

    // Ordenamos tareas para mostrar primero pendientes y próximas.
    val sortedTasks = tasks.sortedWith { a, b ->
        compareTasksList(a, b)
    }

    // Aplicamos el filtro seleccionado.
    val filteredTasks = when (selectedFilter) {
        TaskFilterType.ALL -> sortedTasks
        TaskFilterType.PENDING -> sortedTasks.filter { !it.completed }
        TaskFilterType.COMPLETED -> sortedTasks.filter { it.completed }
    }

    // Contadores superiores.
    val total = tasks.size
    val pending = tasks.count { !it.completed }
    val completed = tasks.count { it.completed }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TasksHeader(
                onBack = onBack,
                total = total,
                pending = pending,
                completed = completed
            )
        }

        item {
            TaskFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
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

        if (filteredTasks.isEmpty()) {
            item {
                EmptyTasksCard(selectedFilter = selectedFilter)
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                PremiumTaskListItem(
                    task = task,
                    onCheckedChange = { checked ->
                        taskRepository.updateTaskCompleted(
                            taskId = task.id,
                            completed = checked,
                            onError = { errorMessage = it }
                        )
                    },
                    onEditClick = {
                        taskToEdit = task
                    },
                    onDeleteClick = {
                        taskToDelete = task
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    // Diálogo para editar una tarea.
    if (taskToEdit != null) {
        val selectedTask = taskToEdit!!

        EditTaskDialog(
            task = selectedTask,
            onDismiss = {
                taskToEdit = null
            },
            onSave = { title, dateText, timeText, priority ->
                taskRepository.updateTask(
                    taskId = selectedTask.id,
                    title = title,
                    priority = priority,
                    dateText = dateText,
                    timeText = timeText,
                    onSuccess = {
                        taskToEdit = null
                        errorMessage = ""
                    },
                    onError = { errorMessage = it }
                )
            }
        )
    }

    // Confirmación antes de borrar una tarea.
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Eliminar tarea",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que quieres eliminar \"${taskToDelete?.title}\"? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTask = taskToDelete

                        if (selectedTask != null) {
                            taskRepository.deleteTask(
                                taskId = selectedTask.id,
                                onError = { errorMessage = it }
                            )
                        }

                        taskToDelete = null
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
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TasksHeader(
    onBack: () -> Unit,
    total: Int,
    pending: Int,
    completed: Int
) {
    // Cabecera con botón volver y resumen de tareas.
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = "Mis tareas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Gestiona todas tus tareas",
                    color = Color(0xFF777777)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskSummaryMiniCard(
                modifier = Modifier.weight(1f),
                value = total.toString(),
                label = "Totales",
                color = Color(0xFFFFF2F5)
            )

            TaskSummaryMiniCard(
                modifier = Modifier.weight(1f),
                value = pending.toString(),
                label = "Pendientes",
                color = Color(0xFFFFF8E9)
            )

            TaskSummaryMiniCard(
                modifier = Modifier.weight(1f),
                value = completed.toString(),
                label = "Hechas",
                color = Color(0xFFEAF8EE)
            )
        }
    }
}

@Composable
private fun TaskSummaryMiniCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    // Tarjeta pequeña del resumen superior.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
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
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskFilterRow(
    selectedFilter: TaskFilterType,
    onFilterSelected: (TaskFilterType) -> Unit
) {
    // Filtros de tareas.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == TaskFilterType.ALL,
            onClick = { onFilterSelected(TaskFilterType.ALL) },
            label = { Text("Todas") }
        )

        FilterChip(
            selected = selectedFilter == TaskFilterType.PENDING,
            onClick = { onFilterSelected(TaskFilterType.PENDING) },
            label = { Text("Pendientes") }
        )

        FilterChip(
            selected = selectedFilter == TaskFilterType.COMPLETED,
            onClick = { onFilterSelected(TaskFilterType.COMPLETED) },
            label = { Text("Completadas") }
        )
    }
}

@Composable
private fun EmptyTasksCard(
    selectedFilter: TaskFilterType
) {
    // Mensaje cuando no hay tareas para el filtro seleccionado.
    val title = when (selectedFilter) {
        TaskFilterType.ALL -> "No tienes tareas todavía."
        TaskFilterType.PENDING -> "No tienes tareas pendientes."
        TaskFilterType.COMPLETED -> "No tienes tareas completadas."
    }

    val subtitle = when (selectedFilter) {
        TaskFilterType.ALL -> "Crea una nueva tarea desde el botón central para empezar."
        TaskFilterType.PENDING -> "Vas al día. No tienes nada pendiente ahora mismo."
        TaskFilterType.COMPLETED -> "Cuando completes una tarea, aparecerá aquí."
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
private fun PremiumTaskListItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Estilo visual según prioridad o si está completada.
    val style = getTaskListStyle(task)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = style.backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Línea lateral de prioridad.
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(76.dp)
                    .background(style.accentColor, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(style.accentColor, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = taskStatusText(task),
                        color = style.accentColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildTaskDateTimeText(task),
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = style.accentColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = style.badgeText,
                        color = style.accentColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Editar tarea",
                        tint = Color(0xFF7A3CFF),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onEditClick() }
                    )

                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Eliminar tarea",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        dateText: String,
        timeText: String,
        priority: String
    ) -> Unit
) {
    // Contexto Android para abrir DatePicker y TimePicker.
    val context = LocalContext.current

    // Estados del formulario de edición.
    var title by remember { mutableStateOf(task.title) }
    var dateText by remember { mutableStateOf(task.dateText) }
    var timeText by remember { mutableStateOf(task.timeText) }
    var selectedPriority by remember { mutableStateOf(task.priority.ifBlank { "Media" }) }
    var errorMessage by remember { mutableStateOf("") }

    // Si la tarea no tiene fecha válida, usamos la fecha actual para abrir el selector.
    val initialCalendar = remember(task.id) {
        dateTextToCalendarTaskScreen(task.dateText) ?: Calendar.getInstance()
    }

    // Selector de fecha.
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dateText = "%02d/%02d/%d".format(dayOfMonth, month + 1, year)
            errorMessage = ""
        },
        initialCalendar.get(Calendar.YEAR),
        initialCalendar.get(Calendar.MONTH),
        initialCalendar.get(Calendar.DAY_OF_MONTH)
    )

    // Selector de hora.
    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            timeText = "%02d:%02d".format(hourOfDay, minute)
            errorMessage = ""
        },
        if (timeText.contains(":")) timeText.substringBefore(":").toIntOrNull() ?: 8 else 8,
        if (timeText.contains(":")) timeText.substringAfter(":").toIntOrNull() ?: 0 else 0,
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Editar tarea",
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
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() }
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Fecha") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePicker.show() }
                ) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Hora") },
                        placeholder = { Text("Opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Quitar hora",
                    color = Color(0xFFFF2D95),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        timeText = ""
                        errorMessage = ""
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Prioridad",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Baja", "Media", "Alta").forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) }
                        )
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

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
                    when {
                        title.trim().isBlank() -> {
                            errorMessage = "El título no puede estar vacío"
                        }

                        dateText.trim().isBlank() -> {
                            errorMessage = "Selecciona una fecha"
                        }

                        else -> {
                            onSave(
                                title.trim(),
                                dateText.trim(),
                                timeText.trim(),
                                selectedPriority
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

// Clase auxiliar para definir el estilo de una tarea.
private data class TaskListStyle(
    val accentColor: Color,
    val backgroundColor: Color,
    val badgeText: String
)

// Devuelve colores y badge según prioridad o si está completada.
private fun getTaskListStyle(task: Task): TaskListStyle {
    return when {
        task.completed -> TaskListStyle(
            accentColor = Color(0xFF34C759),
            backgroundColor = Color(0xFFEAF8EE),
            badgeText = "HECHA"
        )

        task.priority == "Alta" -> TaskListStyle(
            accentColor = Color(0xFFFF4D5A),
            backgroundColor = Color(0xFFFFEEF1),
            badgeText = "ALTA"
        )

        task.priority == "Media" -> TaskListStyle(
            accentColor = Color(0xFFFFA000),
            backgroundColor = Color(0xFFFFF8E7),
            badgeText = "MEDIA"
        )

        task.priority == "Baja" -> TaskListStyle(
            accentColor = Color(0xFF34C759),
            backgroundColor = Color(0xFFEAF8EE),
            badgeText = "BAJA"
        )

        else -> TaskListStyle(
            accentColor = Color.Gray,
            backgroundColor = Color(0xFFF7F7F7),
            badgeText = "TAREA"
        )
    }
}

// Texto de estado de una tarea.
private fun taskStatusText(task: Task): String {
    return if (task.completed) {
        "Completada"
    } else {
        "Prioridad: ${task.priority}"
    }
}

// Construye el texto de fecha y hora.
private fun buildTaskDateTimeText(task: Task): String {
    val date = if (task.dateText.isBlank()) {
        "Sin fecha"
    } else {
        formatDateForDisplayTasks(task.dateText)
    }

    val time = if (task.timeText.isBlank()) {
        "Sin hora"
    } else {
        task.timeText
    }

    return "$date · $time"
}

// Ordena tareas por estado, fecha, hora y prioridad.
private fun compareTasksList(a: Task, b: Task): Int {
    if (a.completed != b.completed) {
        return if (!a.completed) -1 else 1
    }

    val dateA = parseTaskDatePartsList(a.dateText)
    val dateB = parseTaskDatePartsList(b.dateText)

    if (dateA.year != dateB.year) return dateA.year.compareTo(dateB.year)
    if (dateA.month != dateB.month) return dateA.month.compareTo(dateB.month)
    if (dateA.day != dateB.day) return dateA.day.compareTo(dateB.day)

    val timeA = parseTimeToMinutesList(a.timeText)
    val timeB = parseTimeToMinutesList(b.timeText)

    if (timeA != timeB) return timeA.compareTo(timeB)

    return priorityValueList(b.priority).compareTo(priorityValueList(a.priority))
}

// Clase auxiliar para fechas.
private data class TaskDatePartsList(
    val year: Int,
    val month: Int,
    val day: Int
)

// Convierte una fecha dd/MM/yyyy en partes numéricas.
private fun parseTaskDatePartsList(dateText: String): TaskDatePartsList {
    val parts = dateText.trim().split("/")

    if (parts.size != 3) {
        return TaskDatePartsList(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }

    val day = parts[0].toIntOrNull() ?: Int.MAX_VALUE
    val month = parts[1].toIntOrNull() ?: Int.MAX_VALUE
    var year = parts[2].toIntOrNull() ?: Int.MAX_VALUE

    if (year != Int.MAX_VALUE && year < 100) {
        year += 2000
    }

    return TaskDatePartsList(year, month, day)
}

// Convierte HH:mm a minutos para ordenar.
private fun parseTimeToMinutesList(timeText: String): Int {
    val parts = timeText.trim().split(":")

    if (parts.size != 2) return Int.MAX_VALUE

    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE
    val minute = parts[1].toIntOrNull() ?: return Int.MAX_VALUE

    return hour * 60 + minute
}

// Valor numérico de prioridad.
private fun priorityValueList(priority: String): Int {
    return when (priority) {
        "Alta" -> 3
        "Media" -> 2
        "Baja" -> 1
        else -> 0
    }
}

// Convierte fecha dd/MM/yyyy a texto más legible.
private fun formatDateForDisplayTasks(dateText: String): String {
    if (dateText.isBlank()) return "Sin fecha"

    val parts = dateText.trim().split("/")

    if (parts.size != 3) return dateText

    val day = parts[0].toIntOrNull() ?: return dateText
    val month = parts[1].toIntOrNull() ?: return dateText
    var year = parts[2].toIntOrNull() ?: return dateText

    if (year < 100) year += 2000

    val monthName = when (month) {
        1 -> "enero"
        2 -> "febrero"
        3 -> "marzo"
        4 -> "abril"
        5 -> "mayo"
        6 -> "junio"
        7 -> "julio"
        8 -> "agosto"
        9 -> "septiembre"
        10 -> "octubre"
        11 -> "noviembre"
        12 -> "diciembre"
        else -> return dateText
    }

    return "$day $monthName $year"
}

// Convierte una fecha dd/MM/yyyy a Calendar.
// Se usa para abrir el selector de fecha en la fecha actual de la tarea.
private fun dateTextToCalendarTaskScreen(dateText: String): Calendar? {
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