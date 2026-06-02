package com.example.scylier.istudyspot.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.scylier.istudyspot.ui.screen.ProfileEditScreen
import com.example.scylier.istudyspot.ui.screen.CustomerServiceScreen
import com.example.scylier.istudyspot.ui.screen.CardCollectionScreen
import com.example.scylier.istudyspot.ui.screen.ViolationScreen
import com.example.scylier.istudyspot.ui.theme.ThemeMode
import com.example.scylier.istudyspot.ui.theme.ThemeState
import com.example.scylier.istudyspot.viewmodel.AiChatViewModel
import com.example.scylier.istudyspot.viewmodel.AuthViewModel
import com.example.scylier.istudyspot.viewmodel.BookingViewModel
import com.example.scylier.istudyspot.viewmodel.CardViewModel
import com.example.scylier.istudyspot.viewmodel.CustomerServiceViewModel
import com.example.scylier.istudyspot.viewmodel.GuideViewModel
import com.example.scylier.istudyspot.viewmodel.HomeViewModel
import com.example.scylier.istudyspot.viewmodel.NotificationViewModel
import com.example.scylier.istudyspot.viewmodel.OrderViewModel
import com.example.scylier.istudyspot.viewmodel.ProfileEditViewModel
import com.example.scylier.istudyspot.viewmodel.ProfileViewModel
import com.example.scylier.istudyspot.viewmodel.RulesViewModel
import com.example.scylier.istudyspot.viewmodel.StudyRecordViewModel
import com.example.scylier.istudyspot.viewmodel.StudyRoomViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = NavRoutes.Home,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configManager = remember { ConfigManager.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val token = configManager.getToken()
    if (token != null) {
        ApiClient.currentToken = token
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
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
                                scope.launch {
                                    snackbarHostState.showSnackbar("请在订单详情中签到")
                                }
                            }
                            "guide" -> navController.navigate(NavRoutes.Guide)
                            "my_booking" -> navController.navigate(NavRoutes.OrderList)
                            "study_record" -> navController.navigate(NavRoutes.StudyRecord)
                            "ai_chat" -> navController.navigate(NavRoutes.CharacterSelect())
                            "notification" -> navController.navigate(NavRoutes.Notification)
                            "settings" -> navController.navigate(NavRoutes.More)
                            "customer_service" -> navController.navigate(NavRoutes.CustomerService)
                        }
                    }
                )
            }

            composable<NavRoutes.StudyRoom>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val viewModel: StudyRoomViewModel = viewModel()
                val studyRoomState by viewModel.studyRoomState.collectAsState()

                LaunchedEffect(Unit) { viewModel.loadStudyRooms() }

                if (studyRoomState.error != null) {
                    LaunchedEffect(studyRoomState.error) {
                        snackbarHostState.showSnackbar(studyRoomState.error!!)
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
                    },
                    onSearch = { keyword -> viewModel.loadStudyRooms(keyword.ifBlank { null }) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.Seat>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Seat>()
                val viewModel: StudyRoomViewModel = viewModel()
                val seatMapState by viewModel.seatMapState.collectAsState()

                LaunchedEffect(args.studyRoomId) { viewModel.loadSeats(args.studyRoomId) }

                if (seatMapState.error != null) {
                    LaunchedEffect(seatMapState.error) {
                        snackbarHostState.showSnackbar(seatMapState.error!!)
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
                            scope.launch {
                                snackbarHostState.showSnackbar("该座位不可预订")
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.Booking>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Booking>()
                val viewModel: BookingViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(state.isSuccess) {
                    if (state.isSuccess && state.orderId != null) {
                        snackbarHostState.showSnackbar("预约成功")
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
                        snackbarHostState.showSnackbar(state.error!!)
                        viewModel.resetState()
                    }
                }

                BookingScreen(
                    studyRoomName = args.studyRoomName,
                    seatPosition = args.seatPosition,
                    pricePerHour = args.pricePerHour,
                    onBook = { startTime, endTime, bookingType ->
                        viewModel.createOrder(args.studyRoomId, args.seatId, startTime, endTime, bookingType)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.Order>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) { backStackEntry ->
                val args = backStackEntry.toRoute<NavRoutes.Order>()
                val viewModel: OrderViewModel = viewModel()
                val state by viewModel.orderDetailState.collectAsState()

                LaunchedEffect(args.orderId) { viewModel.loadOrderDetail(args.orderId) }

                LaunchedEffect(state.actionSuccess) {
                    if (state.actionSuccess != null) {
                        snackbarHostState.showSnackbar(state.actionSuccess!!)
                        viewModel.clearActionSuccess()
                    }
                }

                LaunchedEffect(state.error) {
                    if (state.error != null) {
                        snackbarHostState.showSnackbar(state.error!!)
                        viewModel.clearActionSuccess()
                    }
                }

                OrderDetailScreen(
                    order = state.order,
                    isLoading = state.isLoading,
                    onCheckin = { viewModel.checkin(args.orderId) },
                    onCheckout = { viewModel.checkout(args.orderId) },
                    onCancel = { viewModel.cancelOrder(args.orderId) },
                    onPay = { viewModel.payOrder(args.orderId) },
                    onRenew = { newEndTime -> viewModel.renewOrder(args.orderId, newEndTime) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.OrderList>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val viewModel: OrderViewModel = viewModel()
                val state by viewModel.orderListState.collectAsState()

                LaunchedEffect(Unit) { viewModel.loadOrders() }

                if (state.error != null) {
                    LaunchedEffect(state.error) {
                        snackbarHostState.showSnackbar(state.error!!)
                    }
                }

                OrderListScreen(
                    orders = state.orders,
                    isLoading = state.isLoading,
                    onOrderClick = { order ->
                        navController.navigate(NavRoutes.Order(orderId = order.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.Profile> {
                val viewModel: ProfileViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(Unit) { viewModel.loadProfile(configManager) }

                ProfileScreen(
                    uiState = state,
                    onAvatarClick = {
                        if (!state.isLoggedIn) {
                            navController.navigate(NavRoutes.Login)
                        }
                    },
                    onOrderListClick = {
                        navController.navigate(NavRoutes.OrderList)
                    },
                    onEditProfile = {
                        navController.navigate(NavRoutes.ProfileEdit)
                    },
                    onStudyRecord = {
                        navController.navigate(NavRoutes.StudyRecord)
                    },
                    onCustomerService = {
                        navController.navigate(NavRoutes.CustomerService)
                    },
                    onCardCollection = {
                        navController.navigate(NavRoutes.CardCollection)
                    },
                    onLogout = {
                        configManager.removeToken()
                        navController.navigate(NavRoutes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onThemeChange = { isDark ->
                        ThemeState.themeMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                        ThemeState.saveThemeTo(configManager)
                    }
                )
            }

            composable<NavRoutes.Login>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
                exitTransition = { fadeOut(tween(150)) },
                popEnterTransition = { fadeIn(tween(150)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
            ) {
                val viewModel: AuthViewModel = viewModel()
                val loginState by viewModel.loginState.collectAsState()

                LaunchedEffect(loginState.isSuccess) {
                    if (loginState.isSuccess) {
                        snackbarHostState.showSnackbar("登录成功")
                        navController.popBackStack()
                        viewModel.resetLoginState()
                    }
                }

                LoginScreen(
                    onLogin = { username, password ->
                        viewModel.login(username, password, configManager)
                    },
                    onRegisterClick = {
                        navController.navigate(NavRoutes.Register)
                    },
                    isLoading = loginState.isLoading,
                    errorMessage = loginState.error
                )
            }

            composable<NavRoutes.Register>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
                exitTransition = { fadeOut(tween(150)) },
                popEnterTransition = { fadeIn(tween(150)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
            ) {
                val viewModel: AuthViewModel = viewModel()
                val registerState by viewModel.registerState.collectAsState()

                LaunchedEffect(registerState.isSuccess) {
                    if (registerState.isSuccess) {
                        snackbarHostState.showSnackbar("注册成功")
                        navController.popBackStack()
                        viewModel.resetRegisterState()
                    }
                }

                RegisterScreen(
                    onRegister = { username, password, confirmPassword, nickname ->
                        viewModel.register(username, password, confirmPassword, nickname, configManager)
                    },
                    isLoading = registerState.isLoading,
                    errorMessage = registerState.error
                )
            }

            composable<NavRoutes.Rules> {
                val rulesViewModel: RulesViewModel = viewModel()
                val rulesState by rulesViewModel.state.collectAsState()

                LaunchedEffect(Unit) { rulesViewModel.loadRules() }

                RulesScreen(ruleItems = rulesState.ruleItems, groupedItems = rulesViewModel.groupedItems, onBack = { navController.popBackStack() })
            }

            composable<NavRoutes.More> {
                val profileViewModel: ProfileViewModel = viewModel()
                MoreScreen(
                    onAction = { title ->
                        when (title) {
                            "预约记录" -> navController.navigate(NavRoutes.OrderList)
                            "成就徽章" -> navController.navigate(NavRoutes.Achievement)
                            "卡牌收藏" -> navController.navigate(NavRoutes.CardCollection)
                            "违规记录" -> navController.navigate(NavRoutes.Violation)
                            "积分兑换", "积分明细" -> navController.navigate(NavRoutes.Points)
                            "帮助中心" -> navController.navigate(NavRoutes.Rules)
                            "学习统计" -> navController.navigate(NavRoutes.StudyRecord)
                            "退出登录" -> {
                                profileViewModel.logout(configManager)
                                scope.launch {
                                    snackbarHostState.showSnackbar("已退出登录")
                                }
                            }
                            else -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("${title}功能开发中")
                                }
                            }
                        }
                    }
                )
            }

            composable<NavRoutes.Guide>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val guideViewModel: GuideViewModel = viewModel()
                val guideState by guideViewModel.state.collectAsState()

                LaunchedEffect(Unit) { guideViewModel.loadGuideInfo() }

                GuideScreen(
                    facilities = guideState.facilities,
                    location = guideState.location,
                    openingHours = guideState.openingHours,
                    contact = guideState.contact,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.StudyRecord>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val studyRecordViewModel: StudyRecordViewModel = viewModel()
                val studyRecordState by studyRecordViewModel.state.collectAsState()

                LaunchedEffect(Unit) { studyRecordViewModel.loadStudyRecords() }

                StudyRecordScreen(viewModel = studyRecordViewModel, onBack = { navController.popBackStack() })
            }

            composable<NavRoutes.Notification>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val notificationViewModel: NotificationViewModel = viewModel()
                val notificationState by notificationViewModel.state.collectAsState()

                LaunchedEffect(Unit) { notificationViewModel.loadNotifications() }

                NotificationScreen(
                    notifications = notificationState.notifications,
                    onAction = { },
                    onBack = { navController.popBackStack() }
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

            composable<NavRoutes.Achievement>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                AchievementScreen(onBack = { navController.popBackStack() })
            }

            composable<NavRoutes.Points>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                PointsScreen(onBack = { navController.popBackStack() })
            }

            composable<NavRoutes.ProfileEdit>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val viewModel: ProfileEditViewModel = viewModel()
                ProfileEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.CustomerService>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val viewModel: CustomerServiceViewModel = viewModel()
                CustomerServiceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.CardCollection>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                val viewModel: CardViewModel = viewModel()
                CardCollectionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<NavRoutes.Violation>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
            ) {
                ViolationScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
