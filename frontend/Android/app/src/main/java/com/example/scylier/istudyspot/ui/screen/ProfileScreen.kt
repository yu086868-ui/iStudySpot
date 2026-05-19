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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scylier.istudyspot.ui.theme.GradientEnd
import com.example.scylier.istudyspot.ui.theme.GradientStart
import com.example.scylier.istudyspot.ui.theme.LocalThemeMode
import com.example.scylier.istudyspot.ui.theme.ThemeMode
import com.example.scylier.istudyspot.ui.theme.ThemeState
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.viewmodel.ProfileUiState
import java.util.Calendar

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onAvatarClick: () -> Unit,
    onOrderListClick: () -> Unit
) {
    val context = LocalContext.current
    val configManager = remember { ConfigManager.getInstance(context) }
    var username by remember { mutableStateOf("未登录") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("未设置") }
    var email by remember { mutableStateOf("未设置") }
    var isLoggedIn by remember { mutableStateOf(false) }
    val themeMode = LocalThemeMode.current

    LaunchedEffect(Unit) {
        val savedToken = configManager.getToken()
        if (savedToken != null) {
            isLoggedIn = true
            configManager.getUsername()?.let { username = it }
            configManager.getNickname()?.let { if (it.isNotEmpty()) nickname = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAvatarClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "头像",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (nickname.isNotEmpty()) nickname else username,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    if (nickname.isNotEmpty()) {
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    if (!isLoggedIn) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "点击登录",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (isLoggedIn) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "Lv.${uiState.studyLevel} ${uiState.studyLevelTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.levelProgress },
                            modifier = Modifier.width(120.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "${uiState.totalStudyHours}h", label = "累计学习")
            StatItem(value = "${uiState.streakDays}天", label = "连续打卡")
            StatItem(value = "${uiState.totalBookings}次", label = "完成预约")
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ProfileInfoRow(icon = Icons.Default.Phone, label = "手机号", value = phone)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoRow(icon = Icons.Default.Email, label = "邮箱", value = email)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    CheckinCalendar()
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MenuItemRow(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "我的订单",
                        onClick = onOrderListClick
                    )
                    MenuDivider()
                    MenuItemRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "我的钱包",
                        onClick = {}
                    )
                    MenuDivider()
                    MenuItemRow(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "学习报告",
                        onClick = {}
                    )
                    MenuDivider()
                    ThemeToggleRow(
                        isDark = themeMode == ThemeMode.DARK,
                        onToggle = { checked ->
                            ThemeState.themeMode = if (checked) ThemeMode.DARK else ThemeMode.LIGHT
                        }
                    )
                    MenuDivider()
                    MenuItemRow(
                        icon = Icons.Default.Settings,
                        label = "偏好设置",
                        onClick = {}
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CheckinCalendar() {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val offset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    val checkedDays = remember {
        val set = mutableSetOf<Int>()
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        for (i in 1..14) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
            if (cal.get(Calendar.MONTH) == month) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    set.add(day)
                }
            }
        }
        set
    }

    Column {
        Text(
            text = "${currentMonth + 1}月打卡",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { dayLabel ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = offset + daysInMonth
        val rowCount = (totalCells + 6) / 7

        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - offset + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val isChecked = day in checkedDays
                            val isToday = day == today

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .background(
                                        when {
                                            isChecked -> Color(0xFF22C55E)
                                            isToday -> Color.Transparent
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeToggleRow(isDark: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "深色模式",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(
            checked = isDark,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
