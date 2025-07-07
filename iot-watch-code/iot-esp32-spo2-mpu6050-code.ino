#include <Wire.h>
#include <U8g2lib.h>
#include "MAX30105.h"
#include "spo2_algorithm.h"
#include <MPU6050_light.h>
#include <math.h>

// OLED & Sensor Setup
U8G2_SH1106_128X64_NONAME_F_HW_I2C u8g2(U8G2_R0, U8X8_PIN_NONE);
MAX30105 particleSensor;
MPU6050 mpu(Wire);

uint32_t irBuffer[100];
uint32_t redBuffer[100];
int32_t spo2, heartRate;
int8_t validSPO2, validHeartRate;

// Function to calculate magnitude and classify activity
// String classifyActivity(float ax, float ay, float az, float gx, float gy, float gz) {
//   float accMag = sqrt(ax * ax + ay * ay + az * az);
//   float gyroMag = sqrt(gx * gx + gy * gy + gz * gz);

//   if (accMag < 0.5 || accMag > 2.5 || gyroMag > 200)
//     return "Fall";
//   else if (accMag > 1.2 || gyroMag > 100)
//     return "Running";
//   else
//     return "Normal";
// }
String classifyActivity(float ax, float ay, float az, float gx, float gy, float gz) {
  float accMag = sqrt(ax * ax + ay * ay + az * az);
  float gyroMag = sqrt(gx * gx + gy * gy + gz * gz);

  if (accMag < 0.5 || accMag > 2.5 || gyroMag > 200) {
    return "Fall";
  } else if (accMag > 1.5 && gyroMag > 80) {
    return "Running";
  } else {
    return "Normal";
  }
}

void setup() {
  Wire.begin(8, 9); // Indus Board GPIOs
  Serial.begin(115200);
  delay(1000);
  u8g2.begin();

  // MAX30105
  particleSensor.begin(Wire, I2C_SPEED_STANDARD);
  particleSensor.setup();
  particleSensor.setPulseAmplitudeRed(0x0A);
  particleSensor.setPulseAmplitudeGreen(0);

  // MPU6050
  mpu.begin();
  mpu.calcOffsets(true, true);
}

void loop() {
  // Read MAX30105 samples
  for (int i = 0; i < 100; i++) {
    while (!particleSensor.available()) particleSensor.check();
    redBuffer[i] = particleSensor.getRed();
    irBuffer[i] = particleSensor.getIR();
    particleSensor.nextSample();
  }

  // Calculate HR and SpO2
  maxim_heart_rate_and_oxygen_saturation(irBuffer, 100, redBuffer,
                                         &spo2, &validSPO2,
                                         &heartRate, &validHeartRate);

  // Read MPU data
  mpu.update();
  float tempC = mpu.getTemp();
  float ax = mpu.getAccX();
  float ay = mpu.getAccY();
  float az = mpu.getAccZ();
  float gx = mpu.getGyroX();
  float gy = mpu.getGyroY();
  float gz = mpu.getGyroZ();

  float accMag = sqrt(ax * ax + ay * ay + az * az);
  float gyroMag = sqrt(gx * gx + gy * gy + gz * gz);
  String activity = classifyActivity(ax, ay, az, gx, gy, gz);

  // === Serial Monitor Output ===
  Serial.println("==== Sensor Readings ====");
  Serial.print("HR: "); Serial.println(validHeartRate ? heartRate : 0);
  Serial.print("SpO2: "); Serial.println(validSPO2 ? spo2 : 0);
  Serial.print("Temp: "); Serial.print(tempC); Serial.println(" C");

  Serial.print("AccX: "); Serial.print(ax, 2);
  Serial.print(" AccY: "); Serial.print(ay, 2);
  Serial.print(" AccZ: "); Serial.print(az, 2);
  Serial.print(" | AccMag: "); Serial.println(accMag, 2);

  Serial.print("GyroX: "); Serial.print(gx, 2);
  Serial.print(" GyroY: "); Serial.print(gy, 2);
  Serial.print(" GyroZ: "); Serial.print(gz, 2);
  Serial.print(" | GyroMag: "); Serial.println(gyroMag, 2);

  Serial.print("Activity Status: "); Serial.println(activity);
  Serial.println("=========================\n");

  // === Serial Plotter Output (CSV-style with labels) ===
  Serial.print("AccX: "); Serial.print(ax, 2); Serial.print(", ");
  Serial.print("AccY: "); Serial.print(ay, 2); Serial.print(", ");
  Serial.print("AccZ: "); Serial.print(az, 2); Serial.print(", ");
  Serial.print("GyroX: "); Serial.print(gx, 2); Serial.print(", ");
  Serial.print("GyroY: "); Serial.print(gy, 2); Serial.print(", ");
  Serial.print("GyroZ: "); Serial.print(gz, 2); Serial.print(", ");
  Serial.print("AccMag: "); Serial.print(accMag, 2); Serial.print(", ");
  Serial.print("GyroMag: "); Serial.print(gyroMag, 2); Serial.print(", ");
  Serial.print("Status: ");
  if (activity == "Fall") Serial.println("2"); // 2 = Fall
  else if (activity == "Running") Serial.println("1"); // 1 = Running
  else Serial.println("0"); // 0 = Normal

  // === OLED Output ===
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_6x12_tr);
  u8g2.setCursor(0, 12);
  u8g2.print("HR: "); u8g2.print(validHeartRate ? heartRate : 0);
  u8g2.setCursor(0, 26);
  u8g2.print("SpO2: "); u8g2.print(validSPO2 ? spo2 : 0);
  u8g2.setCursor(0, 40);
  u8g2.print("Temp: "); u8g2.print(tempC, 1); u8g2.print(" C");
  u8g2.setCursor(0, 54);
  u8g2.print("Act: "); u8g2.print(activity);
  u8g2.sendBuffer();

  delay(1000);
}
