package com.example.scylier.istudyspot.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.order.OrderItem
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.ui.screen.BookingScreen
import com.example.scylier.istudyspot.ui.screen.GuideScreen
import com.example.scylier.istudyspot.ui.screen.HomeScreen
import com.example.scylier.istudyspot.ui.screen.LoginScreen
import com.example.scylier.istudyspot.ui.screen.MoreScreen
import com.example.scylier.istudyspot.ui.screen.NotificationScreen
import com.example.scylier.istudyspot.ui.screen.OrderDetailScreen
import com.example.scylier.istudyspot.ui.screen.OrderListScreen
import com.example.scylier.istudyspot.ui.screen.ProfileScreen
import com.example.scylier.istudyspot.ui.screen.RegisterScreen
import com.example.scylier.istudyspot.ui.screen.RulesScreen
import com.example.scylier.istudyspot.ui.screen.SeatMapScreen
import com.example.scylier.istudyspot.ui.screen.StudyRecordScreen
import com.example.scylier.istudyspot.ui.screen.StudyRoomScreen
import com.example.scylier.istudyspot.ui.theme.IStudySpotTheme
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.ui.screen.AiChatScreen
import com.example.scylier.istudyspot.viewmodel.AiChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = NavRoutes.Home,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { MainRepository(context) }
    val configManager = remember { ConfigManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    IStudySpotTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable<NavRoutes.Home> {
                HomeScreen(
                    onAction = { actionId ->
                        when (actionId) {
                            "booking" -> navController.navigate(NavRoutes.StudyRoom)
                            "checkin" -> {
                                navController.navigate(NavRoutes.OrderList)
                                Toast.makeText(context, "请在订单详情中签到", Toast.LENGTH_SHORT).show()
                            }
                            "guide" -> navController.navigate(NavRoutes.Guide)
                            "my_booking" -> navController.navigate(NavRoutes.OrderList)
                            "study_record" -> navController.navigate(NavRoutes.StudyRecord)
                            "ai_chat" -> navController.navigate(NavRoutes.AiChat)
                            "notification" -> navController.navigate(NavRoutes.Notification)
                            "settings" -> navController.navigate(NavRoutes.More)
                        }
                    }
                )
            }

            composable<NavRoutes.StudyRoom> {
                var studyRooms by remember { mutableStateOf<List<StudyRoomItem>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val response = repository.getStudyRooms()
                    when (response) {
                        is ApiResponse.Success -> {
                            studyRooms = response.data.list
                            isLoading = false
                        }
                        is ApiResponse.Error -> {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            studyRooms = emptyList()
                            isLoading = false
                        }
                    }
                }

                StudyRoomScreen(
                    studyRooms = studyRooms,
                    isLoading = isLoading,
                    onStudyRoomClick = { room ->
                        navController.navigate(
                            NavRoutes.Seat(
                                studyRoomId = room.id,
                                studyRoomName = room.name
                            )
                        )
                    }
                )
            }

            composable<NavRoutes.Seat> { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Seat>()
                var seats by remember { mutableStateOf<List<SeatInfo>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(args.studyRoomId) {
                    val response = repository.getStudyRoomSeats(args.studyRoomId)
                    when (response) {
                        is ApiResponse.Success -> {
                            seats = response.data.seats
                            isLoading = false
                        }
                        is ApiResponse.Error -> {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            seats = emptyList()
                            isLoading = false
                        }
                    }
                }

                SeatMapScreen(
                    studyRoomName = args.studyRoomName,
                    seats = seats,
                    isLoading = isLoading,
                    onSeatClick = { seat ->
                        if (seat.status == "available") {
                            navController.navigate(
                                NavRoutes.Booking(
                                    seatId = seat.id,
                                    studyRoomId = args.studyRoomId,
                                    studyRoomName = args.studyRoomName,
                                    seatPosition = "${seat.row}-${seat.col}",
                                    pricePerHour = seat.pricePerHour
                                )
                            )
                        } else {
                            Toast.makeText(context, "该座位不可预订", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            composable<NavRoutes.Booking> { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Booking>()

                BookingScreen(
                    studyRoomName = args.studyRoomName,
                    seatPosition = args.seatPosition,
                    pricePerHour = args.pricePerHour,
                    onBook = { startTime, endTime, bookingType ->
                        val token = configManager.getToken()
                        if (token == null) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@BookingScreen
                        }

                        scope.launch {
                            val response = repository.createOrder(
                                args.studyRoomId,
                                args.seatId,
                                startTime,
                                endTime,
                                bookingType,
                                token
                            )
                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show()
                                        navController.navigate(
                                            NavRoutes.Order(orderId = response.data.id)
                                        ) {
                                            popUpTo(NavRoutes.Home) { inclusive = false }
                                        }
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )
            }

            composable<NavRoutes.Order> { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Order>()
                var order by remember { mutableStateOf<com.example.scylier.istudyspot.models.order.OrderDetail?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(args.orderId) {
                    val token = configManager.getToken()
                    if (token == null) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@LaunchedEffect
                    }
                    val response = repository.getOrderDetail(args.orderId, token)
                    when (response) {
                        is ApiResponse.Success -> {
                            order = response.data
                            isLoading = false
                        }
                        is ApiResponse.Error -> {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            order = null
                            isLoading = false
                        }
                    }
                }

                OrderDetailScreen(
                    order = order,
                    isLoading = isLoading,
                    onCheckin = {
                        val token = configManager.getToken()
                        if (token == null) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@OrderDetailScreen
                        }
                        scope.launch {
                            val response = repository.checkin(args.orderId, "123456", token)
                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        Toast.makeText(context, "签到成功", Toast.LENGTH_SHORT).show()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    onCheckout = {
                        val token = configManager.getToken()
                        if (token == null) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@OrderDetailScreen
                        }
                        scope.launch {
                            val response = repository.checkout(args.orderId, token)
                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        Toast.makeText(context, "签退成功", Toast.LENGTH_SHORT).show()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    onCancel = {
                        val token = configManager.getToken()
                        if (token == null) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@OrderDetailScreen
                        }
                        scope.launch {
                            val response = repository.cancelOrder(args.orderId, token)
                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        Toast.makeText(context, "取消成功", Toast.LENGTH_SHORT).show()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )
            }

            composable<NavRoutes.OrderList> {
                var orders by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val token = configManager.getToken()
                    if (token == null) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        orders = emptyList()
                        isLoading = false
                        return@LaunchedEffect
                    }
                    val response = repository.getUserOrders(token = token)
                    when (response) {
                        is ApiResponse.Success -> {
                            orders = response.data.list
                            isLoading = false
                        }
                        is ApiResponse.Error -> {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            orders = emptyList()
                            isLoading = false
                        }
                    }
                }

                OrderListScreen(
                    orders = orders,
                    isLoading = isLoading,
                    onOrderClick = { order ->
                        navController.navigate(NavRoutes.Order(orderId = order.id))
                    }
                )
            }

            composable<NavRoutes.Profile> {
                ProfileScreen(
                    onAvatarClick = {
                        navController.navigate(NavRoutes.Login)
                    },
                    onOrderListClick = {
                        navController.navigate(NavRoutes.OrderList)
                    }
                )
            }

            composable<NavRoutes.Login> {
                LoginScreen(
                    onLogin = { username, password ->
                        if (username.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
                            return@LoginScreen
                        }

                        scope.launch {
                            val apiManager = ApiManager(context = context)
                            val response = apiManager.login(username, password)

                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        configManager.saveToken(response.data.token)
                                        configManager.saveUserId(response.data.user.id)
                                        configManager.saveUsername(response.data.user.username)
                                        configManager.saveNickname(response.data.user.nickname)

                                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(NavRoutes.Register)
                    }
                )
            }

            composable<NavRoutes.Register> {
                RegisterScreen(
                    onRegister = { username, password, confirmPassword, nickname ->
                        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                            Toast.makeText(context, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
                            return@RegisterScreen
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                            return@RegisterScreen
                        }

                        scope.launch {
                            val apiManager = ApiManager(context = context)
                            val response = apiManager.register(username, password, nickname)

                            withContext(Dispatchers.Main) {
                                when (response) {
                                    is ApiResponse.Success -> {
                                        configManager.saveToken(response.data.token)
                                        configManager.saveUserId(response.data.user.id)
                                        configManager.saveUsername(response.data.user.username)
                                        configManager.saveNickname(response.data.user.nickname)

                                        Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                    is ApiResponse.Error -> {
                                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )
            }

            composable<NavRoutes.Rules> {
                RulesScreen()
            }

            composable<NavRoutes.More> {
                MoreScreen(
                    onAction = { title ->
                        when (title) {
                            "预约记录" -> navController.navigate(NavRoutes.OrderList)
                            "帮助中心" -> navController.navigate(NavRoutes.Rules)
                            else -> Toast.makeText(context, "${title}功能开发中", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            composable<NavRoutes.Guide> {
                GuideScreen()
            }

            composable<NavRoutes.StudyRecord> {
                StudyRecordScreen()
            }

            composable<NavRoutes.Notification> {
                NotificationScreen()
            }

            composable<NavRoutes.AiChat> {
                val viewModel = remember { AiChatViewModel() }
                val messagesList = viewModel.messages
                val isLoading by viewModel.isLoading.collectAsState()

                AiChatScreen(
                    messages = messagesList,
                    isLoading = isLoading,
                    onSendMessage = { message ->
                        viewModel.sendMessage(message)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
