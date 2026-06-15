package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.scylier.istudyspot.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    studyRoomName: String,
    seatLabel: String,
    pricePerHour: Double,
    onBook: (String, String, String) -> Unit,
    onBack: () -> Unit = {}
) {
    var startCalendar by remember { mutableStateOf<Calendar?>(null) }
    var endCalendar by remember { mutableStateOf<Calendar?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var pendingStartMillis by remember { mutableStateOf<Long?>(null) }
    var pendingEndMillis by remember { mutableStateOf<Long?>(null) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val submitFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val startTime = startCalendar?.let { dateFormatter.format(it.time) } ?: ""
    val endTime = endCalendar?.let { dateFormatter.format(it.time) } ?: ""
    val durationHours = if (startCalendar != null && endCalendar != null) {
        val diff = endCalendar!!.timeInMillis - startCalendar!!.timeInMillis
        if (diff > 0) diff / (1000.0 * 60 * 60) else null
    } else {
        null
    }

    val isStartTimeValid = startCalendar?.let { it.timeInMillis > System.currentTimeMillis() } ?: false
    val isTimeRangeValid = durationHours != null
    val isDurationValid = durationHours?.let { it >= 0.5 && it <= 12.0 } ?: false
    val isFormValid = startCalendar != null &&
        endCalendar != null &&
        isStartTimeValid &&
        isTimeRangeValid &&
        isDurationValid

    val totalPrice = durationHours?.let { hours -> hours * pricePerHour }

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

    if (showStartTimePicker) {
        WheelTimePickerDialog(
            title = "选择开始时间",
            initialHour = startCalendar?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = startCalendar?.get(Calendar.MINUTE)?.roundToHalfHourMinute() ?: 0,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                pendingStartMillis?.let { millis ->
                    val selected = combineDateAndTime(millis, hour, minute)
                    startCalendar = selected
                    if (endCalendar == null || endCalendar!!.timeInMillis <= selected.timeInMillis) {
                        endCalendar = selected.copyCalendar().apply { add(Calendar.MINUTE, 30) }
                    }
                }
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        WheelTimePickerDialog(
            title = "选择结束时间",
            initialHour = endCalendar?.get(Calendar.HOUR_OF_DAY)
                ?: startCalendar?.get(Calendar.HOUR_OF_DAY)
                ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = endCalendar?.get(Calendar.MINUTE)?.roundToHalfHourMinute() ?: 30,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                pendingEndMillis?.let { millis ->
                    endCalendar = combineDateAndTime(millis, hour, minute)
                }
                showEndTimePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppTopBar(title = "预约座位", onBack = onBack)
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
                BookingInfoRow(label = "座位编号", value = seatLabel)
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoRow(
                    label = "单价",
                    value = "￥$pricePerHour/小时",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        BookingTimeField(
            label = "开始时间",
            value = startTime,
            onClick = { showStartDatePicker = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        BookingTimeField(
            label = "结束时间",
            value = endTime,
            onClick = { showEndDatePicker = true }
        )

        if (durationHours != null && totalPrice != null) {
            Spacer(modifier = Modifier.height(20.dp))
            BookingPriceCard(
                durationHours = durationHours,
                pricePerHour = pricePerHour,
                totalPrice = totalPrice
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        BookingValidationMessages(
            hasStartTime = startCalendar != null,
            isStartTimeValid = isStartTimeValid,
            hasTimeRange = startCalendar != null && endCalendar != null,
            isTimeRangeValid = isTimeRangeValid,
            durationHours = durationHours,
            isDurationValid = isDurationValid
        )

        Button(
            onClick = {
                val start = startCalendar
                val end = endCalendar
                if (start != null && end != null) {
                    onBook(submitFormatter.format(start.time), submitFormatter.format(end.time), "hour")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = isFormValid,
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
private fun BookingTimeField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun BookingPriceCard(
    durationHours: Double,
    pricePerHour: Double,
    totalPrice: Double
) {
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
                value = String.format(Locale.getDefault(), "%.1f 小时", durationHours),
                valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            BookingInfoRow(
                label = "单价",
                value = "￥$pricePerHour/小时",
                valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(8.dp))
            BookingInfoRow(
                label = "预估总价",
                value = "￥${String.format(Locale.getDefault(), "%.2f", totalPrice)}",
                valueColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BookingValidationMessages(
    hasStartTime: Boolean,
    isStartTimeValid: Boolean,
    hasTimeRange: Boolean,
    isTimeRangeValid: Boolean,
    durationHours: Double?,
    isDurationValid: Boolean
) {
    if (hasStartTime && !isStartTimeValid) {
        BookingErrorText("开始时间必须晚于当前时间")
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (hasTimeRange && !isTimeRangeValid) {
        BookingErrorText("结束时间必须晚于开始时间")
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (durationHours != null && !isDurationValid) {
        BookingErrorText("单次预约时长需在 0.5 到 12 小时之间")
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BookingErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun WheelTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute.roundToHalfHourMinute()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberWheel(
                        values = (0..23).toList(),
                        selectedValue = selectedHour,
                        formatter = { "%02d".format(it) },
                        onValueChange = { selectedHour = it }
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NumberWheel(
                        values = listOf(0, 30),
                        selectedValue = selectedMinute,
                        formatter = { "%02d".format(it) },
                        onValueChange = { selectedMinute = it }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberWheel(
    values: List<Int>,
    selectedValue: Int,
    formatter: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    )

    LaunchedEffect(listState.firstVisibleItemIndex, values) {
        values.getOrNull(listState.firstVisibleItemIndex)?.let(onValueChange)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .width(84.dp)
            .height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(values) { value ->
            val selected = value == selectedValue
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onValueChange(value) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ) {
                Text(
                    text = formatter(value),
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = if (selected) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

private fun combineDateAndTime(dateMillisUtc: Long, hour: Int, minute: Int): Calendar {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateMillisUtc
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utc.get(Calendar.YEAR))
        set(Calendar.MONTH, utc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute.roundToHalfHourMinute())
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun Calendar.copyCalendar(): Calendar {
    return Calendar.getInstance().also { it.timeInMillis = timeInMillis }
}

private fun Int.roundToHalfHourMinute(): Int {
    return if (this < 15 || this >= 45) 0 else 30
}
