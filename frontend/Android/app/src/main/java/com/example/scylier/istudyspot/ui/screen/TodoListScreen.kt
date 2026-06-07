package com.example.scylier.istudyspot.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

    // 错误提示
    state.error?.let { error ->
        LaunchedEffect(error) {
            // 简单处理：3秒后自动清除
            kotlinx.coroutines.delay(3000)
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
        1 -> Color(0xFFE53935) // 高 - 红色
        2 -> Color(0xFFFB8C00) // 中 - 橙色
        else -> Color(0xFF43A047) // 低 - 绿色
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
            // 优先级指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 勾选框
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

            // 内容
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
                            text = todo.dueTime!!.take(16),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // 操作按钮
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

@Composable
fun AddEditTodoDialog(
    todo: Todo? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, priority: Int, dueTime: String?) -> Unit
) {
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var selectedPriority by remember { mutableStateOf(todo?.priority ?: 2) }
    var dueTime by remember { mutableStateOf(todo?.dueTime ?: "") }
    var titleError by remember { mutableStateOf(false) }

    val isEdit = todo != null

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

                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("截止时间（可选，格式：2026-06-05 18:00:00）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@TextButton
                    }
                    onConfirm(title.trim(), selectedPriority, dueTime.ifBlank { null })
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
    val borderColor by animateColorAsState(
        if (selected) color else MaterialTheme.colorScheme.outlineVariant,
        label = "border"
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
