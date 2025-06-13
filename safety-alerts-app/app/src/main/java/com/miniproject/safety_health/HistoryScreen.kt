//package com.miniproject.safety_health
//
//import android.os.Build
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.Description
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QuerySnapshot
//import kotlinx.coroutines.tasks.await
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class SensorData(
//    val heartRate: Int,
//    val spo2: Int,
//    val temperature: Float,
//    val accelX: Float,
//    val accelY: Float,
//    val accelZ: Float,
//    val gyroX: Float,
//    val gyroY: Float,
//    val gyroZ: Float,
//    val timestamp: Long = System.currentTimeMillis()
//)
//
//@RequiresApi(Build.VERSION_CODES.Q)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HistoryScreen() {
//    val context = LocalContext.current
//    val auth = FirebaseAuth.getInstance()
//    val uid = auth.currentUser?.uid ?: "anonymous"
//    val firestore = FirebaseFirestore.getInstance()
//    val vitalsCollection = firestore
//        .collection("users")
//        .document(uid)
//        .collection("vitals")
//
//    var history by remember { mutableStateOf<List<SensorData>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        try {
//            val snapshot: QuerySnapshot = vitalsCollection
//                .orderBy("timestamp")
//                .get()
//                .await()
//            history = snapshot.documents.mapNotNull { it.toObject(SensorData::class.java) }
//        } catch (e: Exception) {
//            Toast.makeText(context, "Load failed: ${e.message}", Toast.LENGTH_SHORT).show()
//        } finally {
//            isLoading = false
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("History") })
//        }
//    ) { padding ->
//        if (isLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding)
//                    .padding(16.dp)
//            ) {
//                // Heart Rate Chart
//                Text("Heart Rate Trend", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(8.dp))
//                AndroidView(
//                    factory = { ctx ->
//                        LineChart(ctx).apply {
//                            layoutParams = ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                300.dp.roundToPx()
//                            )
//                            description = Description().apply { text = "" }
//                        }
//                    },
//                    update = { chart ->
//                        val entries = history.mapIndexed { idx, data ->
//                            Entry(idx.toFloat(), data.heartRate.toFloat())
//                        }
//                        val set = LineDataSet(entries, "Heart Rate").apply {
//                            setDrawCircles(false)
//                            lineWidth = 2f
//                        }
//                        chart.data = LineData(set)
//                        chart.invalidate()
//                    }
//                )
//
//                Spacer(Modifier.height(24.dp))
//
//                // SpO2 Chart
//                Text("SpO₂ Trend", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(8.dp))
//                AndroidView(
//                    factory = { ctx ->
//                        LineChart(ctx).apply {
//                            layoutParams = ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                300.dp.roundToPx()
//                            )
//                            description = Description().apply { text = "" }
//                        }
//                    },
//                    update = { chart ->
//                        val entries = history.mapIndexed { idx, data ->
//                            Entry(idx.toFloat(), data.spo2.toFloat())
//                        }
//                        val set = LineDataSet(entries, "SpO₂").apply {
//                            setDrawCircles(false)
//                            lineWidth = 2f
//                        }
//                        chart.data = LineData(set)
//                        chart.invalidate()
//                    }
//                )
//
//                // Optionally add more for temperature etc.
//            }
//        }
//    }
//}