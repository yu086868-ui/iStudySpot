package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.models.studyroom.SeatInfo

@Composable
fun SeatMapScreen(
    studyRoomName: String,
    seats: List<SeatInfo>,
    isLoading: Boolean,
    onSeatClick: (SeatInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = studyRoomName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SeatLegend(color = Color(0xFF4CAF50), label = "空闲")
            SeatLegend(color = Color(0xFFFF9800), label = "已预订")
            SeatLegend(color = Color(0xFFF44336), label = "使用中")
            SeatLegend(color = Color(0xFF9E9E9E), label = "不可用")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(seats) { seat ->
                    SeatItem(
                        seat = seat,
                        onClick = { onSeatClick(seat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun SeatItem(
    seat: SeatInfo,
    onClick: () -> Unit
) {
    val backgroundColor = when (seat.status) {
        "available" -> Color(0xFF4CAF50)
        "booked" -> Color(0xFFFF9800)
        "in_use" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    val clickable = seat.status == "available"

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${seat.row}-${seat.col}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
