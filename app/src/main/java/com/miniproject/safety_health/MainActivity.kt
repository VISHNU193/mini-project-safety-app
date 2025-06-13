package com.miniproject.safety_health

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.miniproject.safety_health.ui.theme.HealthMonitorTheme

sealed class Screen(val route: String, val icon: Int, val title: String) {
    object Dashboard : Screen("dashboard", R.drawable.ic_dashboard, "Dashboard")
    object Alerts : Screen("alerts", R.drawable.ic_alert, "Alerts")
    object Chatbot : Screen("chatbot", R.drawable.ic_chat, "Chatbot")
//    object History : Screen("history", R.drawable.ic_history, "History")
}

class MainActivity : ComponentActivity() {
    private val authViewModel = AuthViewModel()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            HealthMonitorTheme {
                val isLoggedIn by remember { derivedStateOf { authViewModel.user != null } }
                if (isLoggedIn) {
                    MainScreen(onLogout = { authViewModel.logout() })
                } else {
                    LoginScreen(viewModel = authViewModel) {
                        // on successful login/register
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Alerts,
        Screen.Chatbot,
    )

    Scaffold(
        topBar = { AppTopBar(onLogout) },
        bottomBar = { BottomNavigationBar(navController, items) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(onLogout) }
            composable(Screen.Alerts.route) { AlertScreen() }
            composable(Screen.Chatbot.route) { ChatbotScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onLogout: () -> Unit) {
    TopAppBar(
        title = { Text("Health Monitor", fontSize = 20.sp) },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Logout"
                )
            }
            IconButton(onClick = { /* TODO: open settings */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavHostController, items: List<Screen>) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.icon),
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
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

//@Composable
//fun DashboardScreen(onLogout: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Dashboard: Live vitals, fall status, last alert")
//    }
//}





