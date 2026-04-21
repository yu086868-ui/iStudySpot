package com.example.scylier.istudyspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scylier.istudyspot.navigation.AppNavigation
import com.example.scylier.istudyspot.navigation.NavRoutes

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            MainScreen(navController = navController)
        }
    }
}

sealed class BottomNavItem(
    val route: NavRoutes,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(NavRoutes.Home, Icons.Default.Home, "首页")
    data object Rules : BottomNavItem(NavRoutes.Rules, Icons.Default.Book, "规则")
    data object More : BottomNavItem(NavRoutes.More, Icons.Default.GridView, "更多")
    data object Profile : BottomNavItem(NavRoutes.Profile, Icons.Default.Person, "我的")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Rules,
    BottomNavItem.More,
    BottomNavItem.Profile
)

@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断当前是否在底部导航页面
    val isBottomNavDestination = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        bottomBar = {
            if (isBottomNavDestination) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
