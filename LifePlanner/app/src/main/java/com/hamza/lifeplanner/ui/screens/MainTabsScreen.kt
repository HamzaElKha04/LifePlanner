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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChartOutlined
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.data.TaskRepository
import java.util.Calendar

private enum class MainTab {
    HOME,
    CALENDAR,
    HABITS,
    PROFILE,
    TASKS
}

@Composable
fun MainTabsScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var bottomBarTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var quickAddInitialDate by rememberSaveable { mutableStateOf("") }

    var taskFilterName by rememberSaveable { mutableStateOf(TaskFilterType.ALL.name) }

    Scaffold(
        containerColor = Color(0xFFF7F7FB),
        bottomBar = {
            PremiumBottomBar(
                selectedTab = bottomBarTab,
                onTabSelected = {
                    currentScreen = it
                    bottomBarTab = it
                },
                onAddClick = {
                    quickAddInitialDate = ""
                    showQuickAddDialog = true
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainTab.HOME -> {
                    HomeScreen(
                        authRepository = authRepository,
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxSize(),
                        onQuickAddClick = {
                            quickAddInitialDate = ""
                            showQuickAddDialog = true
                        },
                        onOpenCalendar = {
                            currentScreen = MainTab.CALENDAR
                            bottomBarTab = MainTab.CALENDAR
                        },
                        onOpenProgress = {
                            currentScreen = MainTab.HABITS
                            bottomBarTab = MainTab.HABITS
                        },
                        onOpenProfile = {
                            currentScreen = MainTab.PROFILE
                            bottomBarTab = MainTab.PROFILE
                        },
                        onOpenAllTasks = {
                            taskFilterName = TaskFilterType.ALL.name
                            currentScreen = MainTab.TASKS
                            bottomBarTab = MainTab.HOME
                        },
                        onOpenPendingTasks = {
                            taskFilterName = TaskFilterType.PENDING.name
                            currentScreen = MainTab.TASKS
                            bottomBarTab = MainTab.HOME
                        },
                        onOpenCompletedTasks = {
                            taskFilterName = TaskFilterType.COMPLETED.name
                            currentScreen = MainTab.TASKS
                            bottomBarTab = MainTab.HOME
                        }
                    )
                }

                MainTab.CALENDAR -> {
                    CalendarScreen(
                        authRepository = authRepository,
                        modifier = Modifier.fillMaxSize(),
                        onQuickAddClick = { selectedDate ->
                            quickAddInitialDate = selectedDate
                            showQuickAddDialog = true
                        }
                    )
                }

                MainTab.HABITS -> {
                    HabitsScreen(
                        authRepository = authRepository,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                MainTab.PROFILE -> {
                    ProfileScreen(
                        authRepository = authRepository,
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                MainTab.TASKS -> {
                    TasksScreen(
                        authRepository = authRepository,
                        initialFilter = TaskFilterType.valueOf(taskFilterName),
                        onBack = {
                            currentScreen = MainTab.HOME
                            bottomBarTab = MainTab.HOME
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showQuickAddDialog) {
            QuickAddTaskDialog(
                authRepository = authRepository,
                initialDate = quickAddInitialDate,
                onDismiss = {
                    showQuickAddDialog = false
                    quickAddInitialDate = ""
                },
                onTaskSaved = {
                    showQuickAddDialog = false
                    quickAddInitialDate = ""
                }
            )
        }
    }
}

@Composable
private fun PremiumBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAddClick: () -> Unit
) {
    val barBackground = Color(0xFFF4F3F8)
    val activeColor = Color(0xFFFF2D95)
    val inactiveColor = Color(0xFF9A9AA5)
    val activePill = Color(0xFFE6E9FF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(barBackground)
            .navigationBarsPadding()
            .height(86.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            BottomBarItem(
                label = "Inicio",
                selected = selectedTab == MainTab.HOME,
                selectedColor = activeColor,
                inactiveColor = inactiveColor,
                activeBackground = activePill,
                onClick = { onTabSelected(MainTab.HOME) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Inicio"
                    )
                }
            )

            BottomBarItem(
                label = "Calendario",
                selected = selectedTab == MainTab.CALENDAR,
                selectedColor = activeColor,
                inactiveColor = inactiveColor,
                activeBackground = activePill,
                onClick = { onTabSelected(MainTab.CALENDAR) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Calendario"
                    )
                }
            )

            Spacer(modifier = Modifier.width(70.dp))

            BottomBarItem(
                label = "Hábitos",
                selected = selectedTab == MainTab.HABITS,
                selectedColor = activeColor,
                inactiveColor = inactiveColor,
                activeBackground = activePill,
                onClick = { onTabSelected(MainTab.HABITS) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.InsertChartOutlined,
                        contentDescription = "Hábitos"
                    )
                }
            )

            BottomBarItem(
                label = "Perfil",
                selected = selectedTab == MainTab.PROFILE,
                selectedColor = activeColor,
                inactiveColor = inactiveColor,
                activeBackground = activePill,
                onClick = { onTabSelected(MainTab.PROFILE) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = "Perfil"
                    )
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp)
                .size(62.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFF8A3D),
                            Color(0xFFFF2D95),
                            Color(0xFF6A00F4)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Nueva tarea",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    inactiveColor: Color,
    activeBackground: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val contentColor = if (selected) selectedColor else inactiveColor

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 34.dp)
                .background(
                    color = if (selected) activeBackground else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            color = contentColor
        )
    }
}

@Composable
private fun QuickAddTaskDialog(
    authRepository: AuthRepository,
    initialDate: String,
    onDismiss: () -> Unit,
    onTaskSaved: () -> Unit
) {
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository() }
    val userId = authRepository.getCurrentUserId()

    val initialCalendar = remember(initialDate) {
        dateTextToCalendarMainTabs(initialDate) ?: Calendar.getInstance()
    }

    var taskText by remember { mutableStateOf("") }
    var taskDate by remember(initialDate) { mutableStateOf(initialDate) }
    var taskTime by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Media") }
    var errorMessage by remember { mutableStateOf("") }

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            taskDate = "%02d/%02d/%d".format(dayOfMonth, month + 1, year)
            errorMessage = ""
        },
        initialCalendar.get(Calendar.YEAR),
        initialCalendar.get(Calendar.MONTH),
        initialCalendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            taskTime = "%02d:%02d".format(hourOfDay, minute)
            errorMessage = ""
        },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Nueva tarea rápida",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = {
                        taskText = it
                        errorMessage = ""
                    },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = taskDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        placeholder = { Text("Selecciona fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable {
                                datePicker.show()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tocar para cambiar fecha",
                    color = Color(0xFF777777)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = taskTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora") },
                        placeholder = { Text("Opcional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable {
                                timePicker.show()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Tocar para elegir hora",
                        color = Color(0xFF777777)
                    )

                    if (taskTime.isNotBlank()) {
                        Text(
                            text = "Quitar hora",
                            color = Color(0xFFFF2D95),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                taskTime = ""
                                errorMessage = ""
                            }
                        )
                    }
                }

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
                        taskText.trim().isBlank() -> {
                            errorMessage = "Escribe un título"
                        }

                        taskDate.trim().isBlank() -> {
                            errorMessage = "Selecciona una fecha"
                        }

                        userId == null -> {
                            errorMessage = "No se ha encontrado el usuario"
                        }

                        else -> {
                            taskRepository.addTask(
                                userId = userId,
                                title = taskText.trim(),
                                priority = selectedPriority,
                                dateText = taskDate.trim(),
                                timeText = taskTime.trim(),
                                onSuccess = { onTaskSaved() },
                                onError = { errorMessage = it }
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

private fun dateTextToCalendarMainTabs(dateText: String): Calendar? {
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