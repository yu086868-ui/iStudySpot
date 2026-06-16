package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.models.agent.AgentReplyBlock
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
import com.example.scylier.istudyspot.repository.AgentConversationSummary
import com.example.scylier.istudyspot.ui.components.AppTopBar
import com.example.scylier.istudyspot.viewmodel.AgentViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    viewModel: AgentViewModel,
    onBack: () -> Unit,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<AgentConversationSummary?>(null) }
    val userMessageIndexes = remember(state.messages) {
        state.messages.mapIndexedNotNull { index, message ->
            if (message.role == AgentMessageRole.USER) index else null
        }
    }
    val currentVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    val previousQuestionIndex = userMessageIndexes.lastOrNull { it < currentVisibleIndex }
    val nextQuestionIndex = userMessageIndexes.firstOrNull { it > currentVisibleIndex }

    LaunchedEffect(Unit) {
        if (!state.hasActiveConversation) {
            viewModel.startNewConversation()
        }
    }

    LaunchedEffect(state.activeConversationId, state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    fun sendInput() {
        if (inputText.isNotBlank() && !state.isExecuting) {
            viewModel.submitPrompt(inputText)
            inputText = ""
            keyboardController?.hide()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AgentHistoryDrawer(
                    conversations = state.conversations,
                    activeConversationId = state.activeConversationId,
                    onNewConversation = {
                        viewModel.startNewConversation()
                        inputText = ""
                        coroutineScope.launch { drawerState.close() }
                    },
                    onConversationClick = { conversation ->
                        viewModel.openConversation(conversation.id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onPinConversation = { viewModel.pinConversation(it.id) },
                    onDeleteConversation = { deleteTarget = it }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "AI Agent",
                    onBack = onBack,
                    actions = {
                        FilledIconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "历史会话"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                AgentInputBar(
                    inputText = inputText,
                    isExecuting = state.isExecuting,
                    onInputChange = { inputText = it },
                    onSend = { sendInput() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (state.messages.isEmpty()) {
                    AgentMinimalEmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        items(state.messages, key = { message -> message.id }) { message ->
                            AgentMessageCard(
                                message = message,
                                viewModel = viewModel,
                                onActionNavigate = onActionNavigate
                            )
                        }
                    }

                    if (userMessageIndexes.size > 1) {
                        AgentQuestionJumpPanel(
                            canJumpUp = previousQuestionIndex != null,
                            canJumpDown = nextQuestionIndex != null,
                            onJumpUp = {
                                previousQuestionIndex?.let { target ->
                                    coroutineScope.launch { listState.animateScrollToItem(target) }
                                }
                            },
                            onJumpDown = {
                                nextQuestionIndex?.let { target ->
                                    coroutineScope.launch { listState.animateScrollToItem(target) }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                        )
                    }
                }
            }
        }
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除会话") },
            text = { Text("确定要删除“${target.title}”吗？删除后无法恢复。") },
            confirmButton = {
                ElevatedButton(
                    onClick = {
                        viewModel.deleteConversation(target.id)
                        deleteTarget = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                ElevatedButton(onClick = { deleteTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AgentQuestionJumpPanel(
    canJumpUp: Boolean,
    canJumpDown: Boolean,
    onJumpUp: () -> Unit,
    onJumpDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = offsetX.roundToInt(),
                    y = offsetY.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "提问",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onJumpUp,
                enabled = canJumpUp,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "上一条提问"
                )
            }
            Button(
                onClick = onJumpDown,
                enabled = canJumpDown,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "下一条提问"
                )
            }
        }
    }
}

@Composable
private fun AgentInputBar(
    inputText: String,
    isExecuting: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("输入你的问题") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 3
            )
            Spacer(modifier = Modifier.size(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isExecuting,
                modifier = Modifier.size(48.dp)
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送"
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentHistoryDrawer(
    conversations: List<AgentConversationSummary>,
    activeConversationId: String?,
    onNewConversation: () -> Unit,
    onConversationClick: (AgentConversationSummary) -> Unit,
    onPinConversation: (AgentConversationSummary) -> Unit,
    onDeleteConversation: (AgentConversationSummary) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "历史会话",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        ElevatedButton(
            onClick = onNewConversation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("新建对话")
        }
        if (conversations.isEmpty()) {
            Text(
                text = "暂无历史会话",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            conversations.forEach { conversation ->
                AgentHistoryDrawerItem(
                    conversation = conversation,
                    selected = conversation.id == activeConversationId,
                    onClick = { onConversationClick(conversation) },
                    onPinConversation = { onPinConversation(conversation) },
                    onDeleteConversation = { onDeleteConversation(conversation) }
                )
            }
        }
    }
}

@Composable
private fun AgentHistoryDrawerItem(
    conversation: AgentConversationSummary,
    selected: Boolean,
    onClick: () -> Unit,
    onPinConversation: () -> Unit,
    onDeleteConversation: () -> Unit
) {
    var menuExpanded by remember(conversation.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationDrawerItem(
            modifier = Modifier.weight(1f),
            label = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.title,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (conversation.isPinned) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "已置顶",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = conversation.preview.ifBlank { "暂无内容" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            selected = selected,
            onClick = onClick
        )
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多操作"
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("置顶") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onPinConversation()
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除会话") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteConversation()
                    }
                )
            }
        }
    }
}

@Composable
private fun AgentMinimalEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI Agent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "可查询自习室、座位、预约和规则。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "直接输入问题开始聊天。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "从右上角可查看历史会话",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentMessageCard(
    message: AgentMessage,
    viewModel: AgentViewModel,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val results = message.results.ifEmpty { listOfNotNull(message.result) }
    var expanded by remember(message.id) {
        mutableStateOf(!results.any { it.isListResult() })
    }

    val isUser = message.role == AgentMessageRole.USER
    val cardColor = when {
        isUser -> MaterialTheme.colorScheme.primary
        message.isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimary
        message.isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 1f)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AgentMessageContent(message = message, color = textColor)
                if (!isUser && results.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AgentResultSummaryBar(
                        summary = resultSummaryText(results),
                        expanded = expanded,
                        onToggle = { expanded = !expanded }
                    )
                    if (expanded) {
                        results.forEachIndexed { index, result ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            AgentResultBlock(
                                result = result,
                                viewModel = viewModel,
                                onActionNavigate = onActionNavigate
                            )
                        }
                    }
                }
                if (!isUser && message.uiAction?.type == "navigate" && !message.uiAction.route.isNullOrBlank()) {
                    if (results.isEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    AgentNavigationActionButton(
                        action = message.uiAction,
                        onActionNavigate = onActionNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentResultSummaryBar(
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summary,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentNavigationActionButton(
    action: AgentUiAction,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    ElevatedButton(
        onClick = { onActionNavigate(action.route.orEmpty(), action.params) }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(readableNavigationAction(action.route.orEmpty()))
    }
}

@Composable
private fun AgentMessageContent(
    message: AgentMessage,
    color: Color
) {
    val blocks = message.blocks
    if (blocks.isEmpty()) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            AgentReplyBlockView(block = block, color = color)
        }
    }
}

@Composable
private fun AgentReplyBlockView(
    block: AgentReplyBlock,
    color: Color
) {
    when (block.type) {
        "bullet" -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "-", color = color, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        "numbered" -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${index + 1}.", color = color, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        else -> {
            val text = block.text?.takeIf { it.isNotBlank() } ?: block.items.joinToString("\n")
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun AgentResultBlock(
    result: AgentToolExecutionResult,
    viewModel: AgentViewModel,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.displayToolName(result.tool),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "v${result.schemaVersion}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (result.tool) {
            "list_study_rooms" -> AgentStudyRoomItems(result, onActionNavigate)
            "get_study_room_detail" -> AgentStudyRoomDetail(result, onActionNavigate)
            "list_room_seats" -> AgentSeatItems(result)
            "get_my_reservations" -> AgentReservationItems(result, onActionNavigate)
            "get_reservation_rules" -> AgentRuleItems(result)
            else -> AgentJsonFallback(result)
        }

        val action = result.uiAction
        if (action?.type == "navigate" && !action.route.isNullOrBlank()) {
            ElevatedButton(
                onClick = { onActionNavigate(action.route, action.params) }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("打开相关页面")
            }
        }
    }
}

@Composable
private fun AgentStudyRoomItems(
    result: AgentToolExecutionResult,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val items = result.data.listOfMaps("items")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { room ->
            val roomId = room.longValue("id")
            val roomName = room.stringValue("name").orEmpty()
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = room.stringValue("address").orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (roomId != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ElevatedButton(
                                onClick = {
                                    onActionNavigate(
                                        "seat_list",
                                        mapOf(
                                            "studyRoomId" to roomId,
                                            "studyRoomName" to roomName
                                        )
                                    )
                                }
                            ) {
                                Text("查看座位")
                            }
                            ElevatedButton(
                                onClick = {
                                    onActionNavigate(
                                        "studyroom_detail",
                                        mapOf(
                                            "studyRoomId" to roomId,
                                            "studyRoomName" to roomName
                                        )
                                    )
                                }
                            ) {
                                Text("查看详情")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentStudyRoomDetail(
    result: AgentToolExecutionResult,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val room = result.data.mapValue("studyRoom")
    val roomId = room.longValue("id")
    val roomName = room.stringValue("name").orEmpty()
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = roomName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = room.stringValue("address").orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (roomId != null) {
                ElevatedButton(
                    onClick = {
                        onActionNavigate(
                            "seat_list",
                            mapOf(
                                "studyRoomId" to roomId,
                                "studyRoomName" to roomName
                            )
                        )
                    }
                ) {
                    Text("查看该自习室座位")
                }
            }
        }
    }
}

@Composable
private fun AgentSeatItems(result: AgentToolExecutionResult) {
    val items = result.data.listOfMaps("items")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { seat ->
            val row = seat["rowNum"]?.toString()
            val col = seat["colNum"]?.toString()
            val label = seat.stringValue("seatNumber")?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(row, col).joinToString("-")
            val status = seat.stringValue("status")?.lowercase().orEmpty()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (label.isBlank()) "--" else "座位 $label",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = readableSeatStatus(status),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (status) {
                        "available", "free", "idle", "1" -> MaterialTheme.colorScheme.primary
                        "booked", "occupied", "in_use" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AgentReservationItems(
    result: AgentToolExecutionResult,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val items = result.data.listOfMaps("items")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { order ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.stringValue("roomName").orEmpty().ifBlank { "预约订单" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "座位 ${order.stringValue("seatNumber") ?: order.stringValue("seatPosition").orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.stringValue("timeRange").orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (items.isNotEmpty()) {
            ElevatedButton(
                onClick = { onActionNavigate("reservation_list", emptyMap()) }
            ) {
                Text("查看完整预约列表")
            }
        }
    }
}

@Composable
private fun AgentRuleItems(result: AgentToolExecutionResult) {
    val data = result.data
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AgentRuleLine("可提前预约", "${data["maxAdvanceDays"] ?: "-"} 天")
        AgentRuleLine("每日预约次数", "${data["maxDailyReservations"] ?: "-"} 次")
        AgentRuleLine("最长时长", "${data["maxDurationHours"] ?: "-"} 小时")
        AgentRuleLine("最短时长", "${data["minDurationMinutes"] ?: "-"} 分钟")
        AgentRuleLine("最晚取消", "开始前 ${data["cancellationDeadlineMinutes"] ?: "-"} 分钟")
        AgentRuleLine("爽约扣分", "${data["noShowPenalty"] ?: "-"} 分")
    }
}

@Composable
private fun AgentRuleLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AgentJsonFallback(result: AgentToolExecutionResult) {
    Text(
        text = result.data.entries.joinToString("\n") { entry -> "${entry.key}: ${entry.value}" },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun AgentToolExecutionResult.isListResult(): Boolean {
    val items = data["items"]
    if (items is List<*>) {
        return items.size > 1
    }
    return tool in setOf("list_study_rooms", "list_room_seats", "get_my_reservations")
}

private fun resultSummaryText(results: List<AgentToolExecutionResult>): String {
    return when {
        results.size > 1 -> "已返回 ${results.size} 个结果"
        results.firstOrNull()?.isListResult() == true -> "已返回列表结果"
        else -> "已返回 1 个结果"
    }
}

private fun readableSeatStatus(status: String): String {
    return when (status) {
        "available", "free", "idle", "1" -> "可用"
        "booked" -> "已预约"
        "occupied", "in_use" -> "使用中"
        else -> status
    }
}

private fun readableNavigationAction(route: String): String {
    return when (route) {
        "study_record" -> "打开学习记录"
        "todo_list" -> "打开学习待办"
        "studyroom_list" -> "打开自习室列表"
        "reservation_list" -> "打开预约列表"
        "reservation_rules" -> "打开预约规则"
        else -> "打开相关页面"
    }
}

private fun Map<String, Any?>.stringValue(key: String): String? {
    return this[key]?.toString()?.takeIf { it.isNotBlank() }
}

private fun Map<String, Any?>.longValue(key: String): Long? {
    return when (val value = this[key]) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}

private fun Map<String, Any?>.mapValue(key: String): Map<String, Any?> {
    @Suppress("UNCHECKED_CAST")
    return this[key] as? Map<String, Any?> ?: emptyMap()
}

private fun Map<String, Any?>.listOfMaps(key: String): List<Map<String, Any?>> {
    @Suppress("UNCHECKED_CAST")
    return this[key] as? List<Map<String, Any?>> ?: emptyList()
}
