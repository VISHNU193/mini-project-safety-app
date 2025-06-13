

# 🕒 Smart Health Monitor – IoT Smartwatch with Mobile Companion App

This project features an **IoT-based smartwatch system** built using **ESP32, MAX30105, MPU6050, and OLED display**, along with an Android **Jetpack Compose-based mobile app** that tracks vital signs, manages emergency contacts, and provides health-related assistance via an AI chatbot.

---

## 📱 Mobile Companion App Overview



The **Health Monitor App** is designed as a frontend interface for the smartwatch. It helps users:

- Monitor real-time health metrics (HR, SpO₂, Temperature)
- Manage emergency contacts
- Interact with an AI-based health assistant chatbot

---

## ⚙️ System Features

### Smartwatch (ESP32 Hardware)
- 💓 Real-time **Heart Rate** and **SpO₂** Monitoring
- 📈 **Temperature** and **Motion Sensing** (MPU6050)
- 🖥️ Live display using SH1106 OLED
- 📟 Serial output for debugging or logging

### Mobile App (Android)
- 🔐 **Secure Authentication** using Firebase
- 🩺 **Live Vital Signs Dashboard**
- 🆘 **Emergency Contact Manager** with cloud sync
- 💬 **AI Chatbot** for health and safety guidance

---

## 🔌 Hardware Components & Setup

| Component          | Model                | Notes                        |
|-------------------|----------------------|------------------------------|
| Microcontroller    | ESP32                | With GPIOs and I2C support   |
| Pulse Oximeter     | MAX30105             | For SpO₂ and heart rate      |
| Motion Sensor      | MPU6050              | For accelerometer & gyro     |
| Display            | SH1106 OLED 128x64   | I2C display module           |
| Misc               | Breadboard, Wires    | For connections              |

### Wiring Connections

| Device      | Pin | ESP32 Pin |
|-------------|-----|-----------|
| MAX30105 / OLED / MPU6050 | SDA | GPIO 8 |
|                              | SCL | GPIO 9 |
|                              | VCC | 3.3V or 5V |
|                              | GND | GND |

📝 *All sensors share the I2C bus on GPIO 8 and GPIO 9.*

---

## 🧰 Hardware Code Setup

### Libraries Required (Install via Arduino Library Manager)

- `U8g2` – OLED display
- `MAX30105` – SparkFun library
- `spo2_algorithm.h` – From SparkFun's examples
- `Adafruit_MPU6050`
- `Adafruit Unified Sensor`

### Upload Instructions

1. Clone or download the code.
2. Open `smartwatch.ino` in Arduino IDE.
3. Select **Board**: ESP32 Dev Module.
4. Ensure `Wire.begin(8, 9)` matches your wiring.
5. Upload to the board and open the **Serial Monitor** at `115200 baud`.

### OLED Display Output

```

HR: 75 bpm
SpO2: 97 %
Temp: 25.4 C
Accel X: 0.02 Y: -0.01
Accel Z: 9.81

````

---

## 📱 Mobile App Setup

### Prerequisites

- Android Studio (Arctic Fox or newer)
- Android SDK 31+
- Firebase project with:
  - Firestore enabled
  - Authentication enabled

### Installation Steps

```bash
git clone https://github.com/yourusername/health-monitor-app.git
````

1. Open project in **Android Studio**
2. Add `google-services.json` from your Firebase console to `app/` directory
3. Sync Gradle & run on a device or emulator

---

## 📲 App Features Explained

### 🔐 Authentication

* Register/login using email and password
* Secure Firebase session management

### 📊 Dashboard

* Displays simulated vital data
* Connected to Firebase Firestore (can be extended to real sensor input)

### 🆘 Emergency Contact Manager

* Add/edit/delete contacts
* Synchronized with Firebase

### 🤖 Health Assistant

* AI chatbot for general wellness guidance
* Real-time chat interface

---

## 📦 Tech Stack (Mobile App)

* **Jetpack Compose**: Modern Android UI
* **MVVM Architecture**
* **Firebase**: Authentication + Firestore
* **Ktor**: API Client
* **Kotlin Coroutines**: Async operations

### Key Files

```
src/
├── main/
│   ├── java/com/miniproject/safety_health/
│   │   ├── dashboardscreen.kt   # Vital signs UI
│   │   ├── alertscreen.kt       # Emergency contact UI
│   │   ├── chatbotscreen.kt     # AI chat interface
│   │   ├── loginscreen.kt       # Auth screens
│   │   ├── models/              # Data models
│   │   └── viewmodels/          # MVVM logic
│   └── res/                     # UI resources
```

---

## 📜 Dependencies

```gradle
// Compose
implementation "androidx.compose.material3:material3:1.2.1"

// Firebase
implementation 'com.google.firebase:firebase-auth-ktx:22.3.1'
implementation 'com.google.firebase:firebase-firestore-ktx:24.11.1'

// Networking
implementation "io.ktor:ktor-client-core:2.3.9"
implementation "io.ktor:ktor-client-cio:2.3.9"

// Async
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'
```

---

## 🛠️ To Use Together (ESP32 + Mobile App)

1. Upload the smartwatch firmware to ESP32.
2. Ensure OLED shows live sensor data.
3. Launch the mobile app.
4. Use dummy or real data (based on your setup).
5. For real-time syncing, integrate Firebase Firestore data writing in ESP32 firmware using Wi-Fi + HTTP or MQTT.

---

## 🧪 Testing

### Mobile App

### Hardware

* Use serial output to verify sensor values.
* Check OLED for real-time display.

---

## 🤝 Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/new-feature`
3. Commit your changes
4. Push and create a Pull Request 🚀

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 🔗 Useful Links

* [Firebase Console](https://firebase.google.com/)
* [U8g2 Library Docs](https://github.com/olikraus/u8g2)

---

**Note**: The app currently simulates vital data. For actual sensor integration, use Wi-Fi/Bluetooth communication from ESP32 to push real sensor values to the cloud or directly to the app.

---

