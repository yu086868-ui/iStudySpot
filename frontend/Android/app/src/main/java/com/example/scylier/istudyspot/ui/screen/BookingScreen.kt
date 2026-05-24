package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    studyRoomName: String,
    seatPosition: String,
    pricePerHour: Double,
    onBook: (String, String, String) -> Unit
) {
    var startCalendar by remember { mutableStateOf<Calendar?>(null) }
    var endCalendar by remember { mutableStateOf<Calendar?>(null) }
    var bookingType by remember { mutableStateOf("hour") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var pendingStartMillis by remember { mutableStateOf<Long?>(null) }
    var pendingEndMillis by remember { mutableStateOf<Long?>(null) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(is24Hour = true)
    val endTimePickerState = rememberTimePickerState(is24Hour = true)

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val startTime = startCalendar?.let { dateFormatter.format(it.time) } ?: ""
    val endTime = endCalendar?.let { dateFormatter.format(it.time) } ?: ""

    val durationHours = if (startCalendar != null && endCalendar != null) {
        val diff = endCalendar!!.timeInMillis - startCalendar!!.timeInMillis
        if (diff > 0) diff / (1000.0 * 60 * 60) else null
    } else null

    val totalPrice = durationHours?.let { it * pricePerHour }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            pendingStartMillis = millis
                            showStartDatePicker = false
                            showStartTimePicker = true
                        }
                    },
                    enabled = startDatePickerState.selectedDateMillis != null
                ) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showStartTimePicker) {
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("选择时间", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = startTimePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                pendingStartMillis?.let { millis ->
                                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = millis
                                    }
                                    startCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utc.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utc.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
                                        set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                                        set(Calendar.MINUTE, startTimePickerState.minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                }
                                showStartTimePicker = false
                            }
                        ) { Text("确认") }
                    }
                }
            }
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            pendingEndMillis = millis
                            showEndDatePicker = false
                            showEndTimePicker = true
                        }
                    },
                    enabled = endDatePickerState.selectedDateMillis != null
                ) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    if (showEndTimePicker) {
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("选择时间", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = endTimePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                pendingEndMillis?.let { millis ->
                                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = millis
                                    }
                                    endCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utc.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utc.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
                                        set(Calendar.HOUR_OF_DAY, endTimePickerState.hour)
                                        set(Calendar.MINUTE, endTimePickerState.minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                }
                                showEndTimePicker = false
                            }
                        ) { Text("确认") }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "预约座位",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "预约信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                BookingInfoRow(label = "自习室", value = studyRoomName)
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoRow(label = "座位", value = seatPosition)
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoRow(
                    label = "单价",
                    value = "¥$pricePerHour/小时",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = startTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("开始时间") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartDatePicker = true },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = endTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("结束时间") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEndDatePicker = true },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "预订类型",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("hour" to "按小时", "half_day" to "半天", "day" to "全天").forEach { (type, label) ->
                FilterChip(
                    selected = bookingType == type,
                    onClick = { bookingType = type },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (durationHours != null && totalPrice != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "费用预估",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BookingInfoRow(
                        label = "时长",
                        value = String.format("%.1f 小时", durationHours),
                        valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BookingInfoRow(
                        label = "单价",
                        value = "¥$pricePerHour/小时",
                        valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(8.dp))
                    BookingInfoRow(
                        label = "预估总价",
                        value = "¥${String.format("%.2f", totalPrice)}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { onBook(startTime, endTime, bookingType) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = startTime.isNotEmpty() && endTime.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("确认预约", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BookingInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    labelColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}
