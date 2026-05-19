package com.example.scylier.istudyspot.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class MoreItemData(
    val title: String,
    val icon: ImageVector,
    val group: String,
    val iconBgColor: Color
)

val moreItems = listOf(
    MoreItemData("预约记录", Icons.AutoMirrored.Filled.ReceiptLong, "预约管理", Color(0xFF3B82F6)),
    MoreItemData("违规记录", Icons.Default.Warning, "预约管理", Color(0xFFEF4444)),
    MoreItemData("学习统计", Icons.Default.Timer, "学习数据", Color(0xFF22C55E)),
    MoreItemData("成就徽章", Icons.Default.Star, "学习数据", Color(0xFFF59E0B)),
    MoreItemData("积分兑换", Icons.Default.CardGiftcard, "积分中心", Color(0xFF8B5CF6)),
    MoreItemData("积分明细", Icons.AutoMirrored.Filled.ReceiptLong, "积分中心", Color(0xFF8B5CF6)),
    MoreItemData("帮助中心", Icons.AutoMirrored.Filled.Help, "其他", Color(0xFF06B6D4)),
    MoreItemData("意见反馈", Icons.Default.Feedback, "其他", Color(0xFF06B6D4)),
    MoreItemData("关于我们", Icons.Default.Info, "其他", Color(0xFF06B6D4)),
    MoreItemData("推荐好友", Icons.Default.GroupAdd, "其他", Color(0xFF06B6D4)),
    MoreItemData("退出登录", Icons.AutoMirrored.Filled.ExitToApp, "其他", Color(0xFFEF4444)),
)

@Composable
fun MoreScreen(onAction: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "更多功能",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val grouped = moreItems.groupBy { it.group }
        grouped.forEach { (group, items) ->
            Text(
                text = group,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
            if (group == "学习数据") {
                AchievementPreview(onAction = onAction)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (group == "积分中心") {
                PointsSummaryCard()
                Spacer(modifier = Modifier.height(12.dp))
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(item.title) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.iconBgColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.iconBgColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AchievementPreview(onAction: (String) -> Unit) {
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
                    text = "我的成就",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.clickable { onAction("成就徽章") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看全部",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementBadge(
                    name = "早起鸟",
                    icon = Icons.Default.WbSunny,
                    color = Color(0xFFF59E0B)
                )
                AchievementBadge(
                    name = "学习达人",
                    icon = Icons.Default.School,
                    color = Color(0xFF3B82F6)
                )
                AchievementBadge(
                    name = "连击王",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun AchievementBadge(name: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PointsSummaryCard() {
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
                    text = "我的积分",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "1,280",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF8B5CF6)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "距下一等级还需720积分",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "1,280 / 2,000",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B5CF6)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { 1280f / 2000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF8B5CF6),
                trackColor = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
