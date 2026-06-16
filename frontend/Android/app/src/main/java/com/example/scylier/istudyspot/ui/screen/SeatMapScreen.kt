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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
        get() = "第${row}排 第${col}列"
}

private fun buildSeatPresentations(layout: SeatLayoutData): Map<Long, SeatPresentation> {
    val seatMap = layout.seats.associateBy { it.id }
    val presentations = mutableMapOf<Long, SeatPresentation>()
    for (item in layout.items) {
        if (item.isSeat && item.seatId != null) {
            seatMap[item.seatId]?.let { seat ->
                presentations[seat.id] = SeatPresentation(seat = seat, row = item.row, col = item.col)
            }
        }
    }
    return presentations
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
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
            SeatLegend(color = extendedColors.warning, label = "已预约")
            SeatLegend(color = MaterialTheme.colorScheme.error, label = "使用中")
            SeatLegend(color = MaterialTheme.colorScheme.onSurfaceVariant, label = "不可用")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureLegend(color = extendedColors.info, label = "电源")
            FeatureLegend(color = MaterialTheme.colorScheme.tertiary, label = "靠窗")
            FeatureLegend(color = extendedColors.warning, label = "推荐", isStar = true)
        }
        if (layout != null && layout.items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LayoutLegend(MaterialTheme.colorScheme.tertiaryContainer, "入口")
                LayoutLegend(MaterialTheme.colorScheme.outlineVariant, "障碍物")
                LayoutLegend(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f), "桌面")
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
                text = "占用 $occupiedCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                layout != null && layout.items.isNotEmpty() -> {
                    ComplexSeatLayout(
                        layout = layout,
                        extendedColors = extendedColors,
                        onSeatClick = { presentation ->
                            if (presentation.seat.status == "available") {
                                selectedSeat = presentation
                            }
                        }
                    )
                }

                else -> {
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
private fun FeatureLegend(color: Color, label: String, isStar: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isStar) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = label,
                modifier = Modifier.size(10.dp),
                tint = color
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
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
    val sortedSeats = remember(seats) { seats.sortedWith(compareBy({ it.row }, { it.col })) }
    val maxRow = sortedSeats.maxOfOrNull { it.row } ?: 1
    val cols = maxCol.coerceAtLeast(1)
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellSize = (maxWidth / cols).coerceIn(36.dp, 56.dp)
            val canvasWidth = cellSize * cols
            val canvasHeight = cellSize * maxRow
            val viewportHeight = if (canvasHeight > 420.dp) 420.dp else canvasHeight + 8.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
                    .height(viewportHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(canvasWidth)
                        .height(canvasHeight)
                ) {
                    sortedSeats.forEach { seat ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = cellSize * (seat.col - 1),
                                    y = cellSize * (seat.row - 1)
                                )
                                .size(cellSize),
                            contentAlignment = Alignment.Center
                        ) {
                            SeatItem(
                                seat = seat,
                                isRecommended = seat.status == "available" && seat.row in 2..4,
                                maxCol = maxCol,
                                extendedColors = extendedColors,
                                baseSize = (cellSize - 10.dp).coerceIn(26.dp, 38.dp),
                                onClick = { onSeatClick(seat) }
                            )
                        }
                    }
                }
            }
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
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = layout.cols.coerceAtLeast(1)
            val rows = layout.rows.coerceAtLeast(1)
            val cellSize = (maxWidth / cols).coerceIn(34.dp, 56.dp)
            val canvasWidth = cellSize * cols
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
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(canvasWidth)
                        .height(canvasHeight)
                ) {
                    layout.items
                        .filter { !it.isSeat && it.itemType == "table" }
                        .forEach { item ->
                            LayoutBlockItem(item = item, cellSize = cellSize, extendedColors = extendedColors)
                        }

                    layout.items
                        .filter { !it.isSeat && it.itemType != "table" }
                        .forEach { item ->
                            LayoutBlockItem(item = item, cellSize = cellSize, extendedColors = extendedColors)
                        }

                    layout.items
                        .filter { it.isSeat && it.seatId != null }
                        .forEach { item ->
                            seatMap[item.seatId]?.let { seat ->
                                val presentation = seatPresentations[seat.id]
                                    ?: SeatPresentation(seat = seat, row = item.row, col = item.col)
                                SeatLayoutSeatItem(
                                    presentation = presentation,
                                    cellSize = cellSize,
                                    extendedColors = extendedColors,
                                    onClick = { onSeatClick(presentation) }
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
    cellSize: Dp,
    extendedColors: ExtendedColors
) {
    if (!shouldRenderLayoutItem(item)) return

    val background = when (item.itemType) {
        "door" -> MaterialTheme.colorScheme.tertiaryContainer
        "pillar" -> MaterialTheme.colorScheme.outlineVariant
        "table" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (item.itemType) {
        "door" -> MaterialTheme.colorScheme.onTertiaryContainer
        "pillar" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val shape = RoundedCornerShape(if (item.itemType == "table") 14.dp else 10.dp)
    val label = when (item.itemType) {
        "door" -> item.label?.takeIf { it.isNotBlank() } ?: "入口"
        "pillar" -> item.label?.takeIf { it.isNotBlank() } ?: "立柱"
        else -> null
    }

    Box(
        modifier = Modifier
            .offset(
                x = cellSize * (item.col - 1),
                y = cellSize * (item.row - 1)
            )
            .zIndex(
                when (item.itemType) {
                    "table" -> 0f
                    "door", "pillar" -> 1f
                    else -> 1f
                }
            )
            .padding(2.dp)
            .width(cellSize * item.width)
            .height(cellSize * item.height)
            .clip(shape)
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun shouldRenderLayoutItem(item: SeatLayoutItemInfo): Boolean {
    return item.itemType == "table" || item.itemType == "door" || item.itemType == "pillar"
}

@Composable
private fun SeatLayoutSeatItem(
    presentation: SeatPresentation,
    cellSize: Dp,
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
            .zIndex(2f)
            .size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        SeatItem(
            seat = presentation.seat,
            isRecommended = presentation.seat.status == "available" &&
                (presentation.seat.isWindow == 1 || presentation.seat.hasPower == 1),
            maxCol = Int.MAX_VALUE,
            extendedColors = extendedColors,
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
    baseSize: Dp = 48.dp,
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

    Box(
        modifier = Modifier
            .width(baseSize)
            .height(baseSize)
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
            text = seat.displayLabel,
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
    coordinateLabel: String,
    maxCol: Int,
    visualCol: Int,
    allowEdgeWindowInference: Boolean,
    extendedColors: ExtendedColors,
    onBookClick: () -> Unit
) {
    val statusText = when (seat.status) {
        "available" -> "空闲"
        "booked" -> "已预约"
        "in_use" -> "使用中"
        else -> "不可用"
    }
    val statusColor = when (seat.status) {
        "available" -> extendedColors.success
        "booked" -> extendedColors.warning
        "in_use" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val isWindowSeat = seat.isWindow == 1 || (allowEdgeWindowInference && (visualCol == 1 || visualCol == maxCol))
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
            text = "座位 ${seat.displayLabel}",
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
                    text = "¥${seat.pricePerHour}/小时",
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
            FeatureChip(
                text = coordinateLabel,
                background = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            features.forEach { feature ->
                FeatureChip(
                    text = feature,
                    background = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        if (seat.status == "available") {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "预约此座位")
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
