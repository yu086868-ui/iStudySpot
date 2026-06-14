package com.example.scylier.istudyspot.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scylier.istudyspot.models.todo.Todo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onBack: () -> Unit,
    viewModel: com.example.scylier.istudyspot.viewmodel.TodoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Todo?>(null) }

    LaunchedEffect(Unit) { viewModel.loadTodos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习待办") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加待办")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.todos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无待办事项",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击右下角 + 添加学习计划",
                        color = MaterialTheme.colorScheme.outlineVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 未完成
                if (state.pendingTodos.isNotEmpty()) {
                    item {
                        Text(
                            "待完成 (${state.pendingTodos.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                        )
                    }
                    items(state.pendingTodos, key = { it.id }) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo.id) },
                            onEdit = { showEditDialog = todo },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }

                // 已完成
                if (state.completedTodos.isNotEmpty()) {
                    item {
                        Text(
                            "已完成 (${state.completedTodos.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 4.dp, top = if (state.pendingTodos.isNotEmpty()) 16.dp else 4.dp)
                        )
                    }
                    items(state.completedTodos, key = { it.id }) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo.id) },
                            onEdit = { showEditDialog = todo },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }
            }
        }
    }

    // 错误提示 Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 添加对话框
    if (showAddDialog) {
        AddEditTodoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, priority, dueTime ->
                viewModel.createTodo(title, priority, dueTime)
                showAddDialog = false
            }
        )
    }

    // 编辑对话框
    showEditDialog?.let { todo ->
        AddEditTodoDialog(
            todo = todo,
            onDismiss = { showEditDialog = null },
            onConfirm = { title, priority, dueTime ->
                viewModel.updateTodo(todo.id, title, priority, dueTime)
                showEditDialog = null
            }
        )
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (todo.priority) {
        1 -> Color(0xFFE53935)
        2 -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }

    val isCompleted = todo.status == "completed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "取消完成" else "标记完成",
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (todo.dueTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDueTime(todo.dueTime!!),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoDialog(
    todo: Todo? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, priority: Int, dueTime: String?) -> Unit
) {
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var selectedPriority by remember { mutableStateOf(todo?.priority ?: 2) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var titleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 编辑模式下，解析已有的 dueTime
    LaunchedEffect(todo?.dueTime) {
        if (todo?.dueTime != null) {
            try {
                val dt = LocalDateTime.parse(todo.dueTime!!, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                selectedDate = dt.toLocalDate()
                selectedTime = dt.toLocalTime()
            } catch (e: Exception) {
                try {
                    val dt = LocalDateTime.parse(todo.dueTime!!)
                    selectedDate = dt.toLocalDate()
                    selectedTime = dt.toLocalTime()
                } catch (_: Exception) {}
            }
        }
    }

    val isEdit = todo != null

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 时间选择器
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.hour ?: 18,
            initialMinute = selectedTime?.minute ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑待办" else "新建待办") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("待办标题") },
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("标题不能为空") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 优先级选择
                Text("优先级", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityChip("高", 1, selectedPriority == 1, Color(0xFFE53935)) { selectedPriority = 1 }
                    PriorityChip("中", 2, selectedPriority == 2, Color(0xFFFB8C00)) { selectedPriority = 2 }
                    PriorityChip("低", 3, selectedPriority == 3, Color(0xFF43A047)) { selectedPriority = 3 }
                }

                // 截止时间选择
                Text("截止时间（可选）", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 日期选择按钮
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedDate?.format(DateTimeFormatter.ofPattern("MM月dd日")) ?: "选择日期",
                            fontSize = 13.sp
                        )
                    }

                    // 时间选择按钮
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "选择时间",
                            fontSize = 13.sp
                        )
                    }

                    // 清除按钮
                    if (selectedDate != null || selectedTime != null) {
                        IconButton(
                            onClick = {
                                selectedDate = null
                                selectedTime = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "清除",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                // 显示已选时间预览
                if (selectedDate != null) {
                    val preview = buildString {
                        append(selectedDate!!.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")))
                        if (selectedTime != null) {
                            append(" ")
                            append(selectedTime!!.format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                    }
                    Text(
                        text = preview,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@TextButton
                    }
                    val dueTimeStr = if (selectedDate != null) {
                        val time = selectedTime ?: LocalTime.of(23, 59)
                        LocalDateTime.of(selectedDate, time)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    } else null
                    onConfirm(title.trim(), selectedPriority, dueTimeStr)
                }
            ) {
                Text(if (isEdit) "保存" else "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun PriorityChip(label: String, value: Int, selected: Boolean, color: Color, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
        label = "bg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else MaterialTheme.colorScheme.outline
        )
    }
}

private fun formatDueTime(dueTime: String): String {
    return try {
        val dt = LocalDateTime.parse(dueTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        dt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    } catch (e: Exception) {
        try {
            val dt = LocalDateTime.parse(dueTime)
            dt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        } catch (e: Exception) {
            dueTime.take(16)
        }
    }
}
