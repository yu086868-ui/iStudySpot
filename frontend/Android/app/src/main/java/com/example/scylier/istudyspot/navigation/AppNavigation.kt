package com.example.scylier.istudyspot.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.ui.screen.AchievementScreen
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
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.ui.screen.AiChatScreen
import com.example.scylier.istudyspot.ui.screen.CharacterSelectScreen
import com.example.scylier.istudyspot.ui.screen.PointsScreen
import com.example.scylier.istudyspot.viewmodel.AiChatViewModel
import com.example.scylier.istudyspot.viewmodel.AuthViewModel
import com.example.scylier.istudyspot.viewmodel.BookingViewModel
import com.example.scylier.istudyspot.viewmodel.GuideViewModel
import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import com.example.scylier.istudyspot.viewmodel.NotificationViewModel
import com.example.scylier.istudyspot.viewmodel.OrderViewModel
import com.example.scylier.istudyspot.viewmodel.ProfileViewModel
import com.example.scylier.istudyspot.viewmodel.RulesViewModel
import com.example.scylier.istudyspot.viewmodel.StudyRecordViewModel
import com.example.scylier.istudyspot.viewmodel.StudyRoomViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = NavRoutes.Home,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configManager = remember { ConfigManager.getInstance(context) }

    val token = configManager.getToken()
    if (token != null) {
        ApiClient.currentToken = token
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<NavRoutes.Home> {
            val homeViewModel: HomeViewModel = viewModel()
            val homeState by homeViewModel.state.collectAsState()

            LaunchedEffect(Unit) { homeViewModel.loadHomeData() }

            HomeScreen(
                uiState = homeState,
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
                        "ai_chat" -> navController.navigate(NavRoutes.CharacterSelect())
                        "notification" -> navController.navigate(NavRoutes.Notification)
                        "settings" -> navController.navigate(NavRoutes.More)
                    }
                }
            )
        }

        composable<NavRoutes.StudyRoom> {
            val viewModel: StudyRoomViewModel = viewModel()
            val studyRoomState by viewModel.studyRoomState.collectAsState()

            LaunchedEffect(Unit) { viewModel.loadStudyRooms() }

            if (studyRoomState.error != null) {
                LaunchedEffect(studyRoomState.error) {
                    Toast.makeText(context, studyRoomState.error, Toast.LENGTH_SHORT).show()
                }
            }

            StudyRoomScreen(
                studyRooms = studyRoomState.studyRooms,
                isLoading = studyRoomState.isLoading,
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
            val viewModel: StudyRoomViewModel = viewModel()
            val seatMapState by viewModel.seatMapState.collectAsState()

            LaunchedEffect(args.studyRoomId) { viewModel.loadSeats(args.studyRoomId) }

            if (seatMapState.error != null) {
                LaunchedEffect(seatMapState.error) {
                    Toast.makeText(context, seatMapState.error, Toast.LENGTH_SHORT).show()
                }
            }

            SeatMapScreen(
                studyRoomName = args.studyRoomName,
                seats = seatMapState.seats,
                isLoading = seatMapState.isLoading,
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
            val viewModel: BookingViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess && state.orderId != null) {
                    Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show()
                    val oid = state.orderId
                    if (oid != null) {
                        navController.navigate(NavRoutes.Order(orderId = oid)) {
                            popUpTo(NavRoutes.Home) { inclusive = false }
                        }
                    }
                    viewModel.resetState()
                }
            }

            LaunchedEffect(state.error) {
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
            }

            BookingScreen(
                studyRoomName = args.studyRoomName,
                seatPosition = args.seatPosition,
                pricePerHour = args.pricePerHour,
                onBook = { startTime, endTime, bookingType ->
                    viewModel.createOrder(args.studyRoomId, args.seatId, startTime, endTime, bookingType)
                }
            )
        }

        composable<NavRoutes.Order> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoutes.Order>()
            val viewModel: OrderViewModel = viewModel()
            val state by viewModel.orderDetailState.collectAsState()

            LaunchedEffect(args.orderId) { viewModel.loadOrderDetail(args.orderId) }

            LaunchedEffect(state.actionSuccess) {
                if (state.actionSuccess != null) {
                    Toast.makeText(context, state.actionSuccess, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionSuccess()
                }
            }

            LaunchedEffect(state.error) {
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionSuccess()
                }
            }

            OrderDetailScreen(
                order = state.order,
                isLoading = state.isLoading,
                onCheckin = { viewModel.checkin(args.orderId) },
                onCheckout = { viewModel.checkout(args.orderId) },
                onCancel = { viewModel.cancelOrder(args.orderId) }
            )
        }

        composable<NavRoutes.OrderList> {
            val viewModel: OrderViewModel = viewModel()
            val state by viewModel.orderListState.collectAsState()

            LaunchedEffect(Unit) { viewModel.loadOrders() }

            if (state.error != null) {
                LaunchedEffect(state.error) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }
            }

            OrderListScreen(
                orders = state.orders,
                isLoading = state.isLoading,
                onOrderClick = { order ->
                    navController.navigate(NavRoutes.Order(orderId = order.id))
                }
            )
        }

        composable<NavRoutes.Profile> {
            val viewModel: ProfileViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) { viewModel.loadProfile(configManager) }

            ProfileScreen(
                uiState = state,
                onAvatarClick = {
                    navController.navigate(NavRoutes.Login)
                },
                onOrderListClick = {
                    navController.navigate(NavRoutes.OrderList)
                }
            )
        }

        composable<NavRoutes.Login> {
            val viewModel: AuthViewModel = viewModel()
            val loginState by viewModel.loginState.collectAsState()

            LaunchedEffect(loginState.isSuccess) {
                if (loginState.isSuccess) {
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    viewModel.resetLoginState()
                }
            }

            LaunchedEffect(loginState.error) {
                if (loginState.error != null) {
                    Toast.makeText(context, loginState.error, Toast.LENGTH_SHORT).show()
                    viewModel.resetLoginState()
                }
            }

            LoginScreen(
                onLogin = { username, password ->
                    viewModel.login(username, password, configManager)
                },
                onRegisterClick = {
                    navController.navigate(NavRoutes.Register)
                }
            )
        }

        composable<NavRoutes.Register> {
            val viewModel: AuthViewModel = viewModel()
            val registerState by viewModel.registerState.collectAsState()

            LaunchedEffect(registerState.isSuccess) {
                if (registerState.isSuccess) {
                    Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    viewModel.resetRegisterState()
                }
            }

            LaunchedEffect(registerState.error) {
                if (registerState.error != null) {
                    Toast.makeText(context, registerState.error, Toast.LENGTH_SHORT).show()
                    viewModel.resetRegisterState()
                }
            }

            RegisterScreen(
                onRegister = { username, password, confirmPassword, nickname ->
                    viewModel.register(username, password, confirmPassword, nickname, configManager)
                }
            )
        }

        composable<NavRoutes.Rules> {
            val rulesViewModel: RulesViewModel = viewModel()
            val rulesState by rulesViewModel.state.collectAsState()

            LaunchedEffect(Unit) { rulesViewModel.loadRules() }

            RulesScreen(ruleItems = rulesState.ruleItems, groupedItems = rulesViewModel.groupedItems)
        }

        composable<NavRoutes.More> {
            val profileViewModel: ProfileViewModel = viewModel()
            MoreScreen(
                onAction = { title ->
                    when (title) {
                        "预约记录" -> navController.navigate(NavRoutes.OrderList)
                        "成就徽章" -> navController.navigate(NavRoutes.Achievement)
                        "积分兑换", "积分明细" -> navController.navigate(NavRoutes.Points)
                        "帮助中心" -> navController.navigate(NavRoutes.Rules)
                        "学习统计" -> navController.navigate(NavRoutes.StudyRecord)
                        "退出登录" -> {
                            profileViewModel.logout(configManager)
                            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(context, "${title}功能开发中", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable<NavRoutes.Guide> {
            val guideViewModel: GuideViewModel = viewModel()
            val guideState by guideViewModel.state.collectAsState()

            LaunchedEffect(Unit) { guideViewModel.loadGuideInfo() }

            GuideScreen(
                facilities = guideState.facilities,
                location = guideState.location,
                openingHours = guideState.openingHours,
                contact = guideState.contact
            )
        }

        composable<NavRoutes.StudyRecord> {
            val studyRecordViewModel: StudyRecordViewModel = viewModel()
            val studyRecordState by studyRecordViewModel.state.collectAsState()

            LaunchedEffect(Unit) { studyRecordViewModel.loadStudyRecords() }

            StudyRecordScreen(viewModel = studyRecordViewModel)
        }

        composable<NavRoutes.Notification> {
            val notificationViewModel: NotificationViewModel = viewModel()
            val notificationState by notificationViewModel.state.collectAsState()

            LaunchedEffect(Unit) { notificationViewModel.loadNotifications() }

            NotificationScreen(
                notifications = notificationState.notifications,
                onAction = { }
            )
        }

        composable<NavRoutes.AiChat> {
            val viewModel: AiChatViewModel = viewModel()
            val messages by viewModel.messages.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val selectedCharacter by viewModel.selectedCharacter.collectAsState()

            AiChatScreen(
                messages = messages,
                isLoading = isLoading,
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                characterName = selectedCharacter?.name,
                characterId = selectedCharacter?.id,
                characterPersona = selectedCharacter?.persona,
                characterAvatarColor = selectedCharacter?.avatarColor,
                onSuggestionClick = { suggestion ->
                    viewModel.sendMessage(suggestion)
                }
            )
        }

        composable<NavRoutes.CharacterSelect> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoutes.CharacterSelect>()
            val viewModel: AiChatViewModel = viewModel()
            val characters by viewModel.characters.collectAsState()

            CharacterSelectScreen(
                characters = characters,
                isLoading = false,
                onSelectCharacter = { character ->
                    viewModel.selectCharacter(character)
                    navController.navigate(NavRoutes.AiChat)
                }
            )
        }

        composable<NavRoutes.Achievement> {
            AchievementScreen()
        }

        composable<NavRoutes.Points> {
            PointsScreen()
        }
    }
}
