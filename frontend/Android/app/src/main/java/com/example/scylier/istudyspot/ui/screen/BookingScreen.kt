package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BookingScreen(
    studyRoomName: String,
    seatPosition: String,
    pricePerHour: Double,
    onBook: (String, String, String) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var bookingType by remember { mutableStateOf("hour") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "预约座位",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "自习室: $studyRoomName",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "座位: $seatPosition",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "单价: ¥$pricePerHour/小时",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("开始时间 (如: 2024-01-01 09:00)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("结束时间 (如: 2024-01-01 12:00)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bookingType,
            onValueChange = { bookingType = it },
            label = { Text("预订类型 (hour/half_day/day)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onBook(startTime, endTime, bookingType) },
            modifier = Modifier.fillMaxWidth(),
            enabled = startTime.isNotEmpty() && endTime.isNotEmpty()
        ) {
            Text("确认预约")
        }
    }
}
