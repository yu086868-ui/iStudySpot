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
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
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
                title = "AI Agent",
                onBack = onBack
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
                        placeholder = { Text("Ask about rooms, seats, reservations, or rules") },
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
                                contentDescription = "Send"
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
                                    text = "AI Agent",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Secure backend tools for rooms, seats, and reservations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Ask for study rooms, seat availability, your reservations, or reservation rules. Sensitive business details stay behind backend checks.",
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
            AgentSectionTitle("Try asking")
        }

        item {
            AgentPromptChips(
                prompts = state.suggestedPrompts,
                onPromptClick = onSuggestionClick
            )
        }

        item {
            AgentSectionTitle("Available tools")
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
                text = "You can ask next",
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
                    text = if (tool.requiresAuth) "Login required" else "Public",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = tool.description?.takeIf { it.isNotBlank() } ?: "Run this agent tool",
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
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (!isUser && message.result != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AgentResultBlock(
                        result = message.result,
                        viewModel = viewModel,
                        onActionNavigate = onActionNavigate
                    )
                }
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
                Text("Open related page")
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
                                Text("View seats")
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
                                Text("Open details")
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
                    Text("View seats for this room")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val row = seat["rowNum"]?.toString()
                val col = seat["colNum"]?.toString()
                val label = seat.stringValue("seatNumber")?.takeIf { it.isNotBlank() }
                    ?: listOfNotNull(row, col).joinToString("-")
                val status = seat.stringValue("status")?.lowercase().orEmpty()
                Text(
                    text = if (label.isBlank()) "Seat" else "Seat $label",
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
                        text = "Seat ${order.stringValue("seatPosition").orEmpty()}",
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
                Text("Open full order list")
            }
        }
    }
}

@Composable
private fun AgentRuleItems(result: AgentToolExecutionResult) {
    val data = result.data
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AgentRuleLine("Advance booking", "${data["maxAdvanceDays"] ?: "-"} days")
        AgentRuleLine("Daily reservations", "${data["maxDailyReservations"] ?: "-"} times")
        AgentRuleLine("Max duration", "${data["maxDurationHours"] ?: "-"} hours")
        AgentRuleLine("Min duration", "${data["minDurationMinutes"] ?: "-"} minutes")
        AgentRuleLine("Cancel before", "${data["cancellationDeadlineMinutes"] ?: "-"} minutes")
        AgentRuleLine("No-show penalty", "${data["noShowPenalty"] ?: "-"} points")
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
        "available", "free", "idle", "1" -> "Available"
        "booked" -> "Booked"
        "occupied", "in_use" -> "In use"
        else -> status
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
