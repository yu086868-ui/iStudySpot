package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            .padding(16.dp)
    ) {
        Text(
            text = "订单详情",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (order != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OrderInfoRow("订单号", order.id)
                    OrderInfoRow("自习室", order.studyRoomName)
                    OrderInfoRow("座位", order.seatPosition)
                    OrderInfoRow("开始时间", order.startTime)
                    OrderInfoRow("结束时间", order.endTime)
                    OrderInfoRow("状态", order.status)
                    OrderInfoRow("总价", "¥${order.totalPrice}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (order.status == "paid") {
                    Button(
                        onClick = onCheckin,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("签到")
                    }
                }

                if (order.status == "in_use") {
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("签退")
                    }
                }

                if (order.status == "paid" || order.status == "pending") {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("取消订单")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
