package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.ui.components.AppTopBar
import com.example.scylier.istudyspot.ui.theme.ExtendedColors
import com.example.scylier.istudyspot.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(
    studyRoomName: String,
    seats: List<SeatInfo>,
    isLoading: Boolean,
    onSeatClick: (SeatInfo) -> Unit,
    onBack: () -> Unit = {}
) {
    var selectedSeat by remember { mutableStateOf<SeatInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxCol = seats.maxOfOrNull { it.col } ?: 6
    val availableCount = seats.count { it.status == "available" }
    val occupiedCount = seats.count { it.status == "in_use" || it.status == "booked" }
    val extendedColors = LocalExtendedColors.current

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        AppTopBar(title = studyRoomName, onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = studyRoomName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SeatLegend(color = extendedColors.success, label = "空闲")
            SeatLegend(color = extendedColors.warning, label = "已预订")
            SeatLegend(color = MaterialTheme.colorScheme.error, label = "使用中")
            SeatLegend(color = MaterialTheme.colorScheme.onSurfaceVariant, label = "不可用")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "总座位 ${seats.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "空闲 $availableCount",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.success
            )
            Text(
                text = "已占 $occupiedCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(seats) { seat ->
                    SeatItem(
                        seat = seat,
                        isRecommended = seat.status == "available" && seat.row in 2..4,
                        maxCol = maxCol,
                        extendedColors = extendedColors,
                        onClick = {
                            if (seat.status == "available") {
                                selectedSeat = seat
                            }
                        }
                    )
                }
            }
        }
    }

    selectedSeat?.let { seat ->
        ModalBottomSheet(
            onDismissRequest = { selectedSeat = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SeatDetailContent(
                seat = seat,
                maxCol = maxCol,
                extendedColors = extendedColors,
                onBookClick = {
                    onSeatClick(seat)
                    selectedSeat = null
                }
            )
        }
    }
}

@Composable
private fun SeatLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeatItem(
    seat: SeatInfo,
    isRecommended: Boolean,
    maxCol: Int,
    extendedColors: ExtendedColors,
    onClick: () -> Unit
) {
    val (backgroundColor, borderColor) = when (seat.status) {
        "available" -> extendedColors.success.copy(alpha = 0.12f) to extendedColors.success
        "booked" -> extendedColors.warning.copy(alpha = 0.12f) to extendedColors.warning
        "in_use" -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val textColor = when (seat.status) {
        "available" -> extendedColors.success
        "booked" -> extendedColors.warning
        "in_use" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderAlpha = if (seat.status == "available") 1f else 0.4f
    val isEnabled = seat.status != "unavailable"
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (isEnabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${seat.row}-${seat.col}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = if (isEnabled) 1f else 0.5f)
        )
        if (isRecommended) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "推荐",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(10.dp),
                tint = extendedColors.warning
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatDetailContent(
    seat: SeatInfo,
    maxCol: Int,
    extendedColors: ExtendedColors,
    onBookClick: () -> Unit
) {
    val areaName = "${('A' + (seat.row - 1) / 2)}区"
    val positionText = "${areaName}-${seat.row}排${seat.col}座"
    val statusText = when (seat.status) {
        "available" -> "空闲"
        "booked" -> "已预订"
        "in_use" -> "使用中"
        else -> "不可用"
    }
    val statusColor = when (seat.status) {
        "available" -> extendedColors.success
        "booked" -> extendedColors.warning
        "in_use" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val isWindowSeat = seat.col == 1 || seat.col == maxCol
    val isVip = seat.type == "vip"
    val features = buildList {
        if (isWindowSeat) add("靠窗")
        if (isVip) add("有电源")
        add("安静区")
    }
    val showRecommend = seat.status == "available" && isWindowSeat

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = positionText,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isVip) extendedColors.warning.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isVip) "VIP座" else "普通座",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isVip) extendedColors.warning else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showRecommend) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "推荐",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "每小时价格",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${seat.pricePerHour}/时",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "座位特点",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            features.forEach { feature ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        if (seat.status == "available") {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "预约此座位")
            }
        }
    }
}
