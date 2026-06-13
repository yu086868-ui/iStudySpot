package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.models.studyroom.SeatLayoutData
import com.example.scylier.istudyspot.models.studyroom.SeatLayoutItemInfo
import com.example.scylier.istudyspot.ui.components.AppTopBar
import com.example.scylier.istudyspot.ui.theme.ExtendedColors
import com.example.scylier.istudyspot.ui.theme.LocalExtendedColors

private data class SeatPresentation(
    val seat: SeatInfo,
    val row: Int,
    val col: Int
) {
    val coordinateLabel: String
        get() = "${row}排${col}列"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(
    studyRoomName: String,
    seats: List<SeatInfo>,
    layout: SeatLayoutData? = null,
    isLoading: Boolean,
    onSeatClick: (SeatInfo) -> Unit,
    onBack: () -> Unit = {}
) {
    var selectedSeat by remember { mutableStateOf<SeatPresentation?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displaySeats = layout?.seats ?: seats
    val maxCol = displaySeats.maxOfOrNull { it.col } ?: 6
    val detailMaxCol = layout?.cols ?: maxCol
    val availableCount = displaySeats.count { it.status == "available" }
    val occupiedCount = displaySeats.count { it.status == "in_use" || it.status == "booked" }
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
        if (layout != null && layout.items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LayoutLegend(MaterialTheme.colorScheme.secondaryContainer, "前台")
                LayoutLegend(MaterialTheme.colorScheme.primaryContainer, "桌面")
                LayoutLegend(MaterialTheme.colorScheme.surfaceVariant, "走道")
                LayoutLegend(extendedColors.infoContainer, "窗边")
                LayoutLegend(MaterialTheme.colorScheme.tertiaryContainer, "入口")
                LayoutLegend(MaterialTheme.colorScheme.outlineVariant, "立柱")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "总座位 ${displaySeats.size}",
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

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (layout != null && layout.items.isNotEmpty()) {
                ComplexSeatLayout(
                    layout = layout,
                    extendedColors = extendedColors,
                    onSeatClick = { presentation ->
                        if (presentation.seat.status == "available") {
                            selectedSeat = presentation
                        }
                    }
                )
            } else {
                LegacySeatGrid(
                    seats = displaySeats,
                    maxCol = maxCol,
                    extendedColors = extendedColors,
                    onSeatClick = { seat ->
                        if (seat.status == "available") {
                            selectedSeat = SeatPresentation(seat = seat, row = seat.row, col = seat.col)
                        }
                    }
                )
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
                seat = seat.seat,
                coordinateLabel = seat.coordinateLabel,
                maxCol = detailMaxCol,
                visualCol = seat.col,
                allowEdgeWindowInference = layout == null,
                extendedColors = extendedColors,
                onBookClick = {
                    onSeatClick(seat.seat)
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
private fun LayoutLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
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
private fun LegacySeatGrid(
    seats: List<SeatInfo>,
    maxCol: Int,
    extendedColors: ExtendedColors,
    onSeatClick: (SeatInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(seats) { seat ->
            SeatItem(
                seat = seat,
                isRecommended = seat.status == "available" && seat.row in 2..4,
                maxCol = maxCol,
                extendedColors = extendedColors,
                widthUnits = 1,
                heightUnits = 1,
                onClick = { onSeatClick(seat) }
            )
        }
    }
}

@Composable
private fun ComplexSeatLayout(
    layout: SeatLayoutData,
    extendedColors: ExtendedColors,
    onSeatClick: (SeatPresentation) -> Unit
) {
    val seatMap = remember(layout.seats) { layout.seats.associateBy { it.id } }
    val seatPresentations = remember(layout) { buildSeatPresentations(layout) }
    val viewportScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = layout.cols.coerceAtLeast(1)
            val rows = layout.rows.coerceAtLeast(1)
            val cellSize = (maxWidth / cols).coerceIn(34.dp, 56.dp)
            val canvasHeight = cellSize * rows
            val viewportHeight = if (canvasHeight > 420.dp) 420.dp else canvasHeight + 8.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
                    .height(viewportHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .verticalScroll(viewportScroll)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(canvasHeight)
                ) {
                    layout.items.forEach { item ->
                        if (item.isSeat && item.seatId != null) {
                            seatMap[item.seatId]?.let { seat ->
                                val presentation = seatPresentations[seat.id]
                                SeatLayoutSeatItem(
                                    presentation = presentation ?: SeatPresentation(seat = seat, row = item.row, col = item.col),
                                    cellSize = cellSize,
                                    extendedColors = extendedColors,
                                    onClick = { onSeatClick(presentation ?: SeatPresentation(seat = seat, row = item.row, col = item.col)) }
                                )
                            }
                        } else {
                            LayoutBlockItem(
                                item = item,
                                cellSize = cellSize,
                                extendedColors = extendedColors
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutBlockItem(
    item: SeatLayoutItemInfo,
    cellSize: androidx.compose.ui.unit.Dp,
    extendedColors: ExtendedColors
) {
    val background = when (item.itemType) {
        "aisle" -> MaterialTheme.colorScheme.surface
        "window" -> MaterialTheme.colorScheme.secondaryContainer
        "door" -> MaterialTheme.colorScheme.tertiaryContainer
        "pillar" -> MaterialTheme.colorScheme.outlineVariant
        "front_desk" -> MaterialTheme.colorScheme.secondaryContainer
        "table" -> MaterialTheme.colorScheme.primaryContainer
        "booth" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
        "lounge_counter" -> extendedColors.warningContainer
        "plant" -> MaterialTheme.colorScheme.primaryContainer
        "zone_label" -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (item.itemType) {
        "aisle" -> MaterialTheme.colorScheme.onSurfaceVariant
        "window" -> MaterialTheme.colorScheme.onSecondaryContainer
        "door" -> MaterialTheme.colorScheme.onTertiaryContainer
        "pillar" -> MaterialTheme.colorScheme.onSurfaceVariant
        "front_desk" -> MaterialTheme.colorScheme.onSecondaryContainer
        "table" -> MaterialTheme.colorScheme.onPrimaryContainer
        "booth" -> MaterialTheme.colorScheme.onTertiaryContainer
        "lounge_counter" -> extendedColors.onWarningContainer
        "plant" -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .offset(
                x = cellSize * (item.col - 1),
                y = cellSize * (item.row - 1)
            )
            .padding(2.dp)
            .width(cellSize * item.width)
            .height(cellSize * item.height)
            .clip(RoundedCornerShape(if (item.itemType == "aisle") 8.dp else 10.dp))
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.label ?: item.itemType,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SeatLayoutSeatItem(
    presentation: SeatPresentation,
    cellSize: androidx.compose.ui.unit.Dp,
    extendedColors: ExtendedColors,
    onClick: () -> Unit
) {
    val markerSize = (cellSize - 10.dp).coerceIn(26.dp, 38.dp)
    Box(
        modifier = Modifier
            .offset(
                x = cellSize * (presentation.col - 1),
                y = cellSize * (presentation.row - 1)
            )
            .size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        SeatItem(
            seat = presentation.seat,
            isRecommended = presentation.seat.status == "available" && (presentation.seat.isWindow == 1 || presentation.seat.hasPower == 1),
            maxCol = Int.MAX_VALUE,
            extendedColors = extendedColors,
            widthUnits = 1,
            heightUnits = 1,
            baseSize = markerSize,
            onClick = onClick
        )
    }
}

@Composable
private fun SeatItem(
    seat: SeatInfo,
    isRecommended: Boolean,
    maxCol: Int,
    extendedColors: ExtendedColors,
    widthUnits: Int,
    heightUnits: Int,
    baseSize: androidx.compose.ui.unit.Dp = 48.dp,
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
    val isEnabled = seat.status == "available"
    val width = baseSize * widthUnits.coerceAtLeast(1)
    val height = baseSize * heightUnits.coerceAtLeast(1)
    val seatLabel = seat.displayLabel

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
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
            text = seatLabel,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = if (isEnabled) 1f else 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
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
        if (seat.hasPower == 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extendedColors.info)
            )
        }
        if (seat.isWindow == 1 && maxCol != Int.MAX_VALUE) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
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
    val positionText = "座位 ${seat.displayLabel}"
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
    val isWindowSeat = seat.isWindow == 1 || seat.col == 1 || seat.col == maxCol
    val isVip = seat.type == "vip"
    val features = buildList {
        if (seat.hasPower == 1) add("有电源")
        if (seat.hasLamp == 1) add("有台灯")
        if (isWindowSeat) add("靠窗")
        if (isVip) add("大桌面")
        seat.description?.takeIf { it.isNotBlank() }?.let { add(it) }
        if (isEmpty()) add("标准座位")
    }
    val showRecommend = seat.status == "available" && (isWindowSeat || seat.hasPower == 1)

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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = seat.coordinateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
