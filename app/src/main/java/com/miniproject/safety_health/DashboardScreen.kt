////package com.miniproject.safety_health
////
////import android.content.Context
////import android.net.ConnectivityManager
////import android.net.Network
////import android.net.NetworkCapabilities
////import android.net.NetworkRequest
////import android.net.wifi.WifiNetworkSpecifier
////import android.os.Build
////import android.widget.Toast
////import androidx.annotation.RequiresApi
////import androidx.compose.animation.animateColor
////import androidx.compose.animation.core.*
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.layout.*
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.scale
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.res.painterResource
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import com.miniproject.safety_health.ui.theme.HealthMonitorTheme
////import io.ktor.client.*
////import io.ktor.client.engine.cio.*
////import io.ktor.client.plugins.contentnegotiation.*
////import io.ktor.client.call.body
////import io.ktor.client.request.*
////import io.ktor.serialization.kotlinx.json.*
////import kotlinx.coroutines.delay
////import kotlinx.serialization.Serializable
////
////// Data model matching ESP32 JSON response
////@Serializable
////data class VitalsResponse(
////    val heartRate: Int,
////    val spo2: Int
////)
////
////@RequiresApi(Build.VERSION_CODES.Q)
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun DashboardScreen(onLogout: () -> Unit) {
////    HealthMonitorTheme {
////        val context = LocalContext.current
////        var connected by remember { mutableStateOf(false) }
////        var heartRate by remember { mutableStateOf(0) }
////        var spo2 by remember { mutableStateOf(0) }
////        var networkCallback: ConnectivityManager.NetworkCallback? by remember { mutableStateOf(null) }
////
////        // HTTP client
////        val client = remember {
////            HttpClient(CIO) {
////                install(ContentNegotiation) {
////                    json()
////                }
////            }
////        }
////
////        // Fetch data when connected
////        LaunchedEffect(connected) {
////            if (connected) {
////                while (connected) {
////                    try {
////                        // Deserialize JSON body into VitalsResponse
////                        val response: VitalsResponse = client.get("http://192.168.4.1/vitals").body()
////                        heartRate = response.heartRate
////                        spo2 = response.spo2
////                    } catch (e: Exception) {
////                        Toast.makeText(context, "Failed to fetch data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
////                        connected = false
////                        break
////                    }
////                    delay(2000)
////                }
////            }
////        }
////
////        Scaffold(
////            topBar = {
////                TopAppBar(
////                    title = { Text("Dashboard") },
////                    actions = {
////                        IconButton(onClick = onLogout) {
////                            Icon(
////                                painter = painterResource(R.drawable.ic_logout),
////                                contentDescription = "Logout",
////                                modifier = Modifier.size(24.dp)
////                            )
////                        }
////                    }
////                )
////            }
////        ) { paddingValues ->
////            Column(
////                modifier = Modifier
////                    .padding(paddingValues)
////                    .fillMaxSize()
////                    .padding(16.dp),
////                horizontalAlignment = Alignment.CenterHorizontally
////            ) {
////                // Watch connection icon
////                Icon(
////                    painter = painterResource(R.drawable.ic_watch),
////                    contentDescription = if (connected) "Disconnect" else "Connect",
////                    tint = if (connected) Color.Green else Color.Red,
////                    modifier = Modifier
////                        .size(48.dp)
////                        .clickable {
////                            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////                            if (!connected) {
////                                val specifier = WifiNetworkSpecifier.Builder()
////                                    .setSsid("ESP32_HealthMonitor")
////                                    .build()
////                                val request = NetworkRequest.Builder()
////                                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
////                                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
////                                    .setNetworkSpecifier(specifier)
////                                    .build()
////                                val callback = object : ConnectivityManager.NetworkCallback() {
////                                    override fun onAvailable(network: Network) {
////                                        super.onAvailable(network)
////                                        connectivityManager.bindProcessToNetwork(network)
////                                        connected = true
////                                        Toast.makeText(context, "Connected to watch", Toast.LENGTH_SHORT).show()
////                                    }
////                                    override fun onLost(network: Network) {
////                                        super.onLost(network)
////                                        connected = false
////                                        Toast.makeText(context, "Watch disconnected", Toast.LENGTH_SHORT).show()
////                                    }
////                                }
////                                networkCallback = callback
////                                connectivityManager.requestNetwork(request, callback)
////                            } else {
////                                networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
////                                connectivityManager.bindProcessToNetwork(null)
////                                connected = false
////                                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
////                            }
////                        }
////                )
////                Text(
////                    text = if (connected) "Connected" else "Disconnected",
////                    color = if (connected) Color.Green else Color.Red,
////                    fontSize = 16.sp,
////                    modifier = Modifier.padding(top = 8.dp)
////                )
////
////                Spacer(Modifier.height(24.dp))
////
////                // Heart animation and value
////                val infiniteTransition = rememberInfiniteTransition()
////                val scale by infiniteTransition.animateFloat(
////                    initialValue = 1f,
////                    targetValue = 1.3f,
////                    animationSpec = infiniteRepeatable(
////                        animation = tween(800, easing = FastOutSlowInEasing),
////                        repeatMode = RepeatMode.Reverse
////                    )
////                )
////                Icon(
////                    painter = painterResource(R.drawable.ic_heartbeat),
////                    contentDescription = "Heart Rate",
////                    tint = MaterialTheme.colorScheme.error,
////                    modifier = Modifier
////                        .size(64.dp)
////                        .scale(scale)
////                )
////                Text("Heart Rate: $heartRate bpm", fontSize = 20.sp)
////
////                Spacer(Modifier.height(32.dp))
////
////                // SpO2 pulse
////                val spo2Color by rememberInfiniteTransition().animateColor(
////                    initialValue = Color.Blue,
////                    targetValue = Color.Cyan,
////                    animationSpec = infiniteRepeatable(
////                        animation = tween(1000),
////                        repeatMode = RepeatMode.Reverse
////                    )
////                )
////                Icon(
////                    painter = painterResource(R.drawable.ic_spo2),
////                    contentDescription = "SpO2",
////                    tint = spo2Color,
////                    modifier = Modifier.size(64.dp)
////                )
////                Text("SpO₂: $spo2%", fontSize = 20.sp)
////            }
////        }
////    }
////}
//package com.miniproject.safety_health
//
//import android.content.Context
//import android.net.*
//import android.net.wifi.ScanResult
//import android.net.wifi.WifiManager
//import android.net.wifi.WifiNetworkSpecifier
//import android.os.Build
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.compose.animation.animateColor
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.miniproject.safety_health.ui.theme.HealthMonitorTheme
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.call.body
//import io.ktor.client.request.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.coroutines.delay
//import kotlinx.serialization.Serializable
//
//// Data model matching ESP32 JSON response
//@Serializable
//data class VitalsResponse(
//    val heartRate: Int,
//    val spo2: Int
//)
//
//@RequiresApi(Build.VERSION_CODES.Q)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardScreen(onLogout: () -> Unit) {
//    HealthMonitorTheme {
//        val context = LocalContext.current
//        var connected by remember { mutableStateOf(false) }
//        var heartRate by remember { mutableStateOf(0) }
//        var spo2 by remember { mutableStateOf(0) }
//        var networkCallback: ConnectivityManager.NetworkCallback? by remember { mutableStateOf(null) }
//        var scanning by remember { mutableStateOf(false) }
//        var availableDevices by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
//
//        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        val client = remember {
//            HttpClient(CIO) {
//                install(ContentNegotiation) {
//                    json()
//                }
//            }
//        }
//
//        // Fetch data when connected
//        LaunchedEffect(connected) {
//            if (connected) {
//                while (connected) {
//                    try {
//                        val response: VitalsResponse = client.get("http://192.168.4.1/vitals").body()
//                        heartRate = response.heartRate
//                        spo2 = response.spo2
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "Failed to fetch data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                        connected = false
//                        break
//                    }
//                    delay(2000)
//                }
//            }
//        }
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Dashboard") },
//                    actions = {
//                        IconButton(onClick = onLogout) {
//                            Icon(
//                                painter = painterResource(R.drawable.ic_logout),
//                                contentDescription = "Logout",
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                )
//            }
//        ) { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .padding(paddingValues)
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Button(onClick = {
//                        if (!scanning) {
//                            scanning = true
//                            availableDevices = wifiManager.scanResults.filter {
//                                it.SSID.startsWith("ESP32")
//                            }
//                            Toast.makeText(context, "Scan complete", Toast.LENGTH_SHORT).show()
//                        }
//                    }) {
//                        Text("Scan Devices")
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(onClick = {
//                        scanning = false
//                        availableDevices = emptyList()
//                        Toast.makeText(context, "Scan stopped", Toast.LENGTH_SHORT).show()
//                    }) {
//                        Text("Stop Scan")
//                    }
//                }
//
//                if (availableDevices.isNotEmpty()) {
//                    Text("Available Devices:", modifier = Modifier.padding(top = 16.dp))
//                    LazyColumn {
//                        items(availableDevices) { device ->
//                            Text(
//                                text = device.SSID,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        val specifier = WifiNetworkSpecifier.Builder()
//                                            .setSsid(device.SSID)
//                                            .build()
//                                        val request = NetworkRequest.Builder()
//                                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                                            .setNetworkSpecifier(specifier)
//                                            .build()
//                                        val callback = object : ConnectivityManager.NetworkCallback() {
//                                            override fun onAvailable(network: Network) {
//                                                super.onAvailable(network)
//                                                connectivityManager.bindProcessToNetwork(network)
//                                                connected = true
//                                                Toast.makeText(context, "Connected to ${device.SSID}", Toast.LENGTH_SHORT).show()
//                                            }
//                                            override fun onLost(network: Network) {
//                                                super.onLost(network)
//                                                connected = false
//                                                Toast.makeText(context, "Disconnected from ${device.SSID}", Toast.LENGTH_SHORT).show()
//                                            }
//                                        }
//                                        networkCallback = callback
//                                        connectivityManager.requestNetwork(request, callback)
//                                    }
//                                    .padding(8.dp)
//                            )
//                        }
//                    }
//                }
//
//                Text(
//                    text = if (connected) "Connected" else "Disconnected",
//                    color = if (connected) Color.Green else Color.Red,
//                    fontSize = 16.sp,
//                    modifier = Modifier.padding(top = 8.dp)
//                )
//
//                Spacer(Modifier.height(24.dp))
//
//                val infiniteTransition = rememberInfiniteTransition()
//                val scale by infiniteTransition.animateFloat(
//                    initialValue = 1f,
//                    targetValue = 1.3f,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(800, easing = FastOutSlowInEasing),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                Icon(
//                    painter = painterResource(R.drawable.ic_heartbeat),
//                    contentDescription = "Heart Rate",
//                    tint = MaterialTheme.colorScheme.error,
//                    modifier = Modifier
//                        .size(64.dp)
//                        .scale(scale)
//                )
//                Text("Heart Rate: $heartRate bpm", fontSize = 20.sp)
//
//                Spacer(Modifier.height(32.dp))
//
//                val spo2Color by rememberInfiniteTransition().animateColor(
//                    initialValue = Color.Blue,
//                    targetValue = Color.Cyan,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(1000),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                Icon(
//                    painter = painterResource(R.drawable.ic_spo2),
//                    contentDescription = "SpO2",
//                    tint = spo2Color,
//                    modifier = Modifier.size(64.dp)
//                )
//                Text("SpO₂: $spo2%", fontSize = 20.sp)
//            }
//        }
//    }
//}


//package com.miniproject.safety_health
//
//import android.Manifest
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.PackageManager
//import android.net.*
//import android.net.wifi.ScanResult
//import android.net.wifi.WifiManager
//import android.net.wifi.WifiNetworkSpecifier
//import android.os.Build
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.compose.animation.animateColor
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import com.miniproject.safety_health.ui.theme.HealthMonitorTheme
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.call.body
//import io.ktor.client.request.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.coroutines.delay
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class VitalsResponse(
//    val heartRate: Int,
//    val spo2: Int
//)
//
//@RequiresApi(Build.VERSION_CODES.Q)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardScreen(onLogout: () -> Unit) {
//    HealthMonitorTheme {
//        val context = LocalContext.current
//        var connected by remember { mutableStateOf(false) }
//        var heartRate by remember { mutableStateOf(0) }
//        var spo2 by remember { mutableStateOf(0) }
//        var networkCallback: ConnectivityManager.NetworkCallback? by remember { mutableStateOf(null) }
//        var scanning by remember { mutableStateOf(false) }
//        var availableDevices by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
//
//        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        val client = remember {
//            HttpClient(CIO) {
//                install(ContentNegotiation) {
//                    json()
//                }
//            }
//        }
//
//        val permissionLauncher = rememberLauncherForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted ->
//            if (!isGranted) {
//                Toast.makeText(context, "Location permission is required for scanning", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        LaunchedEffect(Unit) {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//        }
//
//        val scanResultsReceiver = rememberUpdatedState(object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
//                    val results = try {
//                        @Suppress("DEPRECATION")
//                        wifiManager.scanResults.filter { it.SSID?.startsWith("ESP32") == true }
//                    } catch (e: SecurityException) {
//                        Toast.makeText(context, "Scan failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                        emptyList()
//                    }
//                    availableDevices = results
//                    scanning = false
//                    Toast.makeText(context, "Scan complete", Toast.LENGTH_SHORT).show()
//                }
//            }
//        })
//
//        DisposableEffect(Unit) {
//            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//            context.registerReceiver(scanResultsReceiver.value, intentFilter)
//            onDispose {
//                context.unregisterReceiver(scanResultsReceiver.value)
//            }
//        }
//
//        LaunchedEffect(connected) {
//            if (connected) {
//                while (connected) {
//                    try {
//                        val response: VitalsResponse = client.get("http://192.168.4.1/vitals").body()
//                        heartRate = response.heartRate
//                        spo2 = response.spo2
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "Failed to fetch data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                        connected = false
//                        break
//                    }
//                    delay(2000)
//                }
//            }
//        }
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Dashboard") },
//                    actions = {
//                        IconButton(onClick = onLogout) {
//                            Icon(
//                                painter = painterResource(R.drawable.ic_logout),
//                                contentDescription = "Logout",
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                )
//            }
//        ) { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .padding(paddingValues)
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Button(onClick = {
//                        if (!scanning) {
//                            scanning = true
//                            val success = wifiManager.startScan()
//                            if (!success) {
//                                scanning = false
//                                Toast.makeText(context, "Scan failed", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }) {
//                        Text("Scan Devices")
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(onClick = {
//                        scanning = false
//                        availableDevices = emptyList()
//                        Toast.makeText(context, "Scan stopped", Toast.LENGTH_SHORT).show()
//                    }) {
//                        Text("Stop Scan")
//                    }
//                }
//
//                if (availableDevices.isNotEmpty()) {
//                    Text("Available Devices:", modifier = Modifier.padding(top = 16.dp))
//                    LazyColumn {
//                        items(availableDevices) { device ->
//                            Text(
//                                text = device.SSID ?: "Unknown",
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        val specifier = WifiNetworkSpecifier.Builder()
//                                            .setSsid(device.SSID)
//                                            .build()
//                                        val request = NetworkRequest.Builder()
//                                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                                            .setNetworkSpecifier(specifier)
//                                            .build()
//                                        val callback = object : ConnectivityManager.NetworkCallback() {
//                                            override fun onAvailable(network: Network) {
//                                                super.onAvailable(network)
//                                                connectivityManager.bindProcessToNetwork(network)
//                                                connected = true
//                                                Toast.makeText(context, "Connected to ${device.SSID}", Toast.LENGTH_SHORT).show()
//                                            }
//                                            override fun onLost(network: Network) {
//                                                super.onLost(network)
//                                                connected = false
//                                                Toast.makeText(context, "Disconnected from ${device.SSID}", Toast.LENGTH_SHORT).show()
//                                            }
//                                        }
//                                        networkCallback = callback
//                                        connectivityManager.requestNetwork(request, callback)
//                                    }
//                                    .padding(8.dp)
//                            )
//                        }
//                    }
//                }
//
//                Text(
//                    text = if (connected) "Connected" else "Disconnected",
//                    color = if (connected) Color.Green else Color.Red,
//                    fontSize = 16.sp,
//                    modifier = Modifier.padding(top = 8.dp)
//                )
//
//                Spacer(Modifier.height(24.dp))
//
//                val infiniteTransition = rememberInfiniteTransition()
//                val scale by infiniteTransition.animateFloat(
//                    initialValue = 1f,
//                    targetValue = 1.3f,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(800, easing = FastOutSlowInEasing),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                Icon(
//                    painter = painterResource(R.drawable.ic_heartbeat),
//                    contentDescription = "Heart Rate",
//                    tint = MaterialTheme.colorScheme.error,
//                    modifier = Modifier
//                        .size(64.dp)
//                        .scale(scale)
//                )
//                Text("Heart Rate: $heartRate bpm", fontSize = 20.sp)
//
//                Spacer(Modifier.height(32.dp))
//
//                val spo2Color by rememberInfiniteTransition().animateColor(
//                    initialValue = Color.Blue,
//                    targetValue = Color.Cyan,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(1000),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                Icon(
//                    painter = painterResource(R.drawable.ic_spo2),
//                    contentDescription = "SpO2",
//                    tint = spo2Color,
//                    modifier = Modifier.size(64.dp)
//                )
//                Text("SpO₂: $spo2%", fontSize = 20.sp)
//            }
//        }
//    }
//}



package com.miniproject.safety_health

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miniproject.safety_health.ui.theme.HealthMonitorTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class SensorData(
    val heartRate: Int,
    val spo2: Int,
    val temperature: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    HealthMonitorTheme {
        val context = LocalContext.current

        // Firebase Firestore setup
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: "anonymous"
        val firestore = FirebaseFirestore.getInstance()
        val vitalsCollection = firestore.collection("users").document(uid).collection("vitals")

        // UI state for latest data
        var latestData by remember { mutableStateOf<SensorData?>(null) }

        // Generate, display & send dummy data continuously
        LaunchedEffect(Unit) {
            while (true) {
                val data = generateDummyData()
                latestData = data
                vitalsCollection
                    .add(data)
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Send failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                delay(2000)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                painter = painterResource(R.drawable.ic_logout),
                                contentDescription = "Logout",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                latestData?.let { data ->
                    // Heart Rate
                    val heartRateAnim = rememberInfiniteTransition().animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_heartbeat),
                        contentDescription = "Heart Rate",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(heartRateAnim.value)
                    )
                    Text(
                        text = "Heart Rate: ${data.heartRate} bpm",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // SpO2
                    val spo2Color = rememberInfiniteTransition().animateColor(
                        initialValue = Color.Blue,
                        targetValue = Color.Cyan,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_spo2),
                        contentDescription = "SpO2",
                        tint = spo2Color.value,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "SpO₂: ${data.spo2}%",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Other vitals
                    Text(
                        text = "Temp: ${String.format("%.1f°C", data.temperature)}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Accel: [${String.format("%.2f", data.accelX)}, ${String.format("%.2f", data.accelY)}, ${String.format("%.2f", data.accelZ)}] m/s²",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Gyro: [${String.format("%.2f", data.gyroX)}, ${String.format("%.2f", data.gyroY)}, ${String.format("%.2f", data.gyroZ)}] °/s",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } ?: run {
                    Text(
                        text = "Initializing data...",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// Helper: generate realistic dummy data
fun generateDummyData(): SensorData {
    val hr = Random.nextInt(60, 100)
    val spo2 = Random.nextInt(95, 100)
    val temp = Random.nextDouble(36.5, 37.5).toFloat()
    val accelX = Random.nextDouble(-0.5, 0.5).toFloat()
    val accelY = Random.nextDouble(-0.5, 0.5).toFloat()
    val accelZ = Random.nextDouble(9.0, 10.0).toFloat()
    val gyroX = Random.nextDouble(-1.0, 1.0).toFloat()
    val gyroY = Random.nextDouble(-1.0, 1.0).toFloat()
    val gyroZ = Random.nextDouble(-1.0, 1.0).toFloat()

    return SensorData(
        heartRate = hr,
        spo2 = spo2,
        temperature = temp,
        accelX = accelX,
        accelY = accelY,
        accelZ = accelZ,
        gyroX = gyroX,
        gyroY = gyroY,
        gyroZ = gyroZ
    )
}
