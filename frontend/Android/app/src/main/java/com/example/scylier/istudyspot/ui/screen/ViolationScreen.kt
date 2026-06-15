package com.example.scylier.istudyspot.ui.screen

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
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.ui.components.AppTopBar
import com.example.scylier.istudyspot.viewmodel.ViolationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationScreen(
    viewModel: ViolationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showAppealDialog by remember { mutableStateOf<Long?>(null) }
    var appealReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadViolations()
    }

    LaunchedEffect(state.appealSuccess) {
        if (state.appealSuccess != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        AppTopBar(title = "违规记录", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.violations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadViolations() }) {
                        Text("重试")
                    }
                }
            }
        } else if (state.violations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无违规记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "继续保持，做一个守规则的学习者！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            state.appealSuccess?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.violations) { violation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (violation.status) {
                                "active" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                "appealing" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                                "appeal_approved" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = violation.typeLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (violation.status) {
                                        "active" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        "appealing" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                        "appeal_approved" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = violation.statusLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (violation.status) {
                                            "active" -> MaterialTheme.colorScheme.error
                                            "appealing" -> MaterialTheme.colorScheme.tertiary
                                            "appeal_approved" -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = violation.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            violation.createdAt?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            violation.appealResult?.let { result ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "申诉结果: $result",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (violation.status == "active") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showAppealDialog = violation.id },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("申诉")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showAppealDialog?.let { violationId ->
        AlertDialog(
            onDismissRequest = { showAppealDialog = null; appealReason = "" },
            title = { Text("提交申诉") },
            text = {
                OutlinedTextField(
                    value = appealReason,
                    onValueChange = { appealReason = it },
                    label = { Text("申诉理由") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (appealReason.isNotBlank()) {
                            viewModel.submitAppeal(violationId, appealReason)
                            showAppealDialog = null
                            appealReason = ""
                        }
                    },
                    enabled = appealReason.isNotBlank()
                ) {
                    Text("提交")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppealDialog = null; appealReason = "" }) {
                    Text("取消")
                }
            }
        )
    }
}
