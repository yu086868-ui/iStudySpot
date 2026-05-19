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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(
    studyRoomName: String,
    seats: List<SeatInfo>,
    isLoading: Boolean,
    onSeatClick: (SeatInfo) -> Unit
) {
    var selectedSeat by remember { mutableStateOf<SeatInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxCol = seats.maxOfOrNull { it.col } ?: 6
    val availableCount = seats.count { it.status == "available" }
    val occupiedCount = seats.count { it.status == "in_use" || it.status == "booked" }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
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
            SeatLegend(color = Color(0xFF22C55E), label = "空闲")
            SeatLegend(color = Color(0xFFF59E0B), label = "已预订")
            SeatLegend(color = Color(0xFFEF4444), label = "使用中")
            SeatLegend(color = Color(0xFF94A3B8), label = "不可用")
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
                color = Color(0xFF22C55E)
            )
            Text(
                text = "已占 $occupiedCount",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFEF4444)
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
                        onClick = { selectedSeat = seat }
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
private fun SeatItem(seat: SeatInfo, isRecommended: Boolean, maxCol: Int, onClick: () -> Unit) {
    val (backgroundColor, borderColor) = when (seat.status) {
        "available" -> Color(0xFF22C55E).copy(alpha = 0.12f) to Color(0xFF22C55E)
        "booked" -> Color(0xFFF59E0B).copy(alpha = 0.12f) to Color(0xFFF59E0B)
        "in_use" -> Color(0xFFEF4444).copy(alpha = 0.12f) to Color(0xFFEF4444)
        else -> Color(0xFF94A3B8).copy(alpha = 0.08f) to Color(0xFF94A3B8)
    }
    val textColor = when (seat.status) {
        "available" -> Color(0xFF22C55E)
        "booked" -> Color(0xFFF59E0B)
        "in_use" -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
    }
    val borderAlpha = if (seat.status == "available") 1f else 0.4f
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
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${seat.row}-${seat.col}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
        if (isRecommended) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(10.dp),
                tint = Color(0xFFF59E0B)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatDetailContent(
    seat: SeatInfo,
    maxCol: Int,
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
        "available" -> Color(0xFF22C55E)
        "booked" -> Color(0xFFF59E0B)
        "in_use" -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
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
                        if (isVip) Color(0xFFF59E0B).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isVip) "VIP座" else "普通座",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isVip) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showRecommend) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "推荐",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6)
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
                    containerColor = Color(0xFF22C55E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "预约此座位")
            }
        }
    }
}
