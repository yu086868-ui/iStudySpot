package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.models.agent.AgentReplyBlock
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
import com.example.scylier.istudyspot.ui.components.AppTopBar
import com.example.scylier.istudyspot.ui.theme.LocalExtendedColors
import com.example.scylier.istudyspot.viewmodel.AgentUiState
import com.example.scylier.istudyspot.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    viewModel: AgentViewModel,
    onBack: () -> Unit,
    onActionNavigate: (route: String, params: Map<String, Any?>) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCatalog()
    }

    LaunchedEffect(state.messages.size, state.suggestedPrompts.size) {
        if (state.messages.isNotEmpty()) {
            val extraItems = if (state.suggestedPrompts.isNotEmpty()) 1 else 0
            listState.animateScrollToItem(state.messages.lastIndex + extraItems)
        }
    }

    fun sendInput() {
        if (inputText.isNotBlank() && !state.isExecuting) {
            viewModel.submitPrompt(inputText)
            inputText = ""
            keyboardController?.hide()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "智能助手",
                onBack = onBack,
                actions = {
                    FilledIconButton(
                        onClick = { viewModel.clearConversation() },
                        enabled = state.messages.isNotEmpty(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "清空会话"
                        )
                    }
                }
            )
        },
        bottomBar = {
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
                        onValueChange = { inputText = it },
                        placeholder = { Text("询问自习室、座位、预约或规则") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendInput() }),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    FilledIconButton(
                        onClick = { sendInput() },
                        enabled = inputText.isNotBlank() && !state.isExecuting,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (state.isExecuting) {
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
    ) { paddingValues ->
        if (state.messages.isEmpty()) {
            AgentEmptyState(
                modifier = Modifier.padding(paddingValues),
                state = state,
                onSuggestionClick = { viewModel.submitPrompt(it) },
                onToolClick = { viewModel.triggerToolShortcut(it) }
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
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

                if (state.suggestedPrompts.isNotEmpty()) {
                    item("suggested_prompts") {
                        AgentSuggestedPromptBlock(
                            prompts = state.suggestedPrompts,
                            onPromptClick = { viewModel.submitPrompt(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentEmptyState(
    modifier: Modifier = Modifier,
    state: AgentUiState,
    onSuggestionClick: (String) -> Unit,
    onToolClick: (AgentToolDefinition) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                listOf(extendedColors.gradientStart, extendedColors.gradientEnd)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(
                                    text = "智能助手",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "查询自习室、座位与预约信息",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "你可以查询可用自习室、座位状态、我的预约和预约规则。预约、取消、支付、签到等操作请在对应页面完成。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.94f)
                        )
                    }
                }
            }
        }

        if (state.catalogError != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            text = state.catalogError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            AgentSectionTitle("可以这样问")
        }

        item {
            AgentPromptChips(
                prompts = state.suggestedPrompts,
                onPromptClick = onSuggestionClick
            )
        }

        item {
            AgentSectionTitle("可用查询")
        }

        if (state.isCatalogLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(state.toolCatalog, key = { tool -> tool.name }) { tool ->
                AgentToolCard(tool = tool, onClick = { onToolClick(tool) })
            }
        }
    }
}

@Composable
private fun AgentSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun AgentPromptChips(
    prompts: List<String>,
    onPromptClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        prompts.forEach { prompt ->
            AssistChip(
                onClick = { onPromptClick(prompt) },
                label = { Text(prompt) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun AgentSuggestedPromptBlock(
    prompts: List<String>,
    onPromptClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "你还可以问",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            AgentPromptChips(
                prompts = prompts,
                onPromptClick = onPromptClick
            )
        }
    }
}

@Composable
private fun AgentToolCard(
    tool: AgentToolDefinition,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.title?.takeIf { it.isNotBlank() } ?: tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (tool.requiresAuth) "需登录" else "公开",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = tool.description?.takeIf { it.isNotBlank() } ?: "执行这项查询",
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
                val results = message.results.ifEmpty { listOfNotNull(message.result) }
                if (!isUser && results.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                    Text(text = "•", color = color, style = MaterialTheme.typography.bodyLarge)
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
        items.take(4).forEach { room ->
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
        items.take(8).forEach { seat ->
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
                    text = if (label.isBlank()) "??" else "?? $label",
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
            if (!row.isNullOrBlank() && !col.isNullOrBlank() && label != "$row-$col") {
                Text(
                    text = "${row}?${col}?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        items.take(4).forEach { order ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.stringValue("roomName").orEmpty(),
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
