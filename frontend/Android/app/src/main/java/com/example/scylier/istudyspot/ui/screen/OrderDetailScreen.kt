package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.order.OrderDetail

@Composable
fun OrderDetailScreen(
    order: OrderDetail?,
    isLoading: Boolean,
    onCheckin: () -> Unit,
    onCheckout: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "订单详情",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (order != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "订单信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        DetailStatusBadge(status = order.status)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderDetailRow(label = "订单号", value = order.id)
                    OrderDetailRow(label = "自习室", value = order.studyRoomName)
                    OrderDetailRow(label = "座位", value = order.seatPosition)
                    OrderDetailRow(label = "开始时间", value = order.startTime)
                    OrderDetailRow(label = "结束时间", value = order.endTime)
                    OrderDetailRow(label = "总价", value = "¥${order.totalPrice}", isHighlight = true)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (order.status == "paid") {
                    Button(
                        onClick = onCheckin,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("签到", style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (order.status == "in_use") {
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E)
                        )
                    ) {
                        Text("签退", style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (order.status == "paid" || order.status == "pending") {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("取消订单", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
            color = if (isHighlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DetailStatusBadge(status: String) {
    val (color, label) = when (status) {
        "pending" -> Color(0xFFF59E0B) to "待支付"
        "paid" -> Color(0xFF3B82F6) to "已支付"
        "in_use" -> Color(0xFF22C55E) to "使用中"
        "completed" -> Color(0xFF94A3B8) to "已完成"
        "cancelled" -> Color(0xFFEF4444) to "已取消"
        else -> Color(0xFF94A3B8) to status
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
