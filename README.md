# MotionData — Smartphone-Based Human Motion Capture & Robotics Dataset Generation

**MotionData** is a native Android application built with Kotlin, Jetpack Compose, Room Database, and the Android Sensor Framework. It transforms a standard smartphone into an IMU motion capture terminal to record human demonstrations for training robots via **imitation learning**, **behavior cloning**, **diffusion policies**, and robotics manipulation research.

Users can strap their smartphone to their wrist or forearm, perform manipulation actions (such as picking, placing, wrist rotation, reaching, and lifting), visualize the 3-axis motion trajectories in real time, and export structured time-series datasets as CSV files for machine learning pipelines.

---

## 1. Key Features

- **Robotics-Inspired Dark UI**: Deep charcoal canvas (`#090D14`), cyan neon accents (`#00E5FF`), crisp typography, and technical telemetry cards.
- **Synchronized 9-DOF / 6-DOF Sensor Engine**:
  - Linear Accelerometer ($m/s^2$)
  - Angular Gyroscope ($rad/s$)
  - Fused Rotation Vector Quaternions ($x, y, z, w$)
- **High-Precision Monotonic Timestamps**: Hardware nanosecond timestamps (`event.timestamp`) normalized to relative seconds ($0.000\text{ s}$) from recording start.
- **Dedicated Sensor Threading**: `SensorEventListener` runs on a dedicated background `HandlerThread` to guarantee zero sample loss and avoid UI frame drops.
- **Real-Time Oscilloscope Visualizer**: Custom Compose Canvas graphs rendering live 3-axis acceleration and angular velocity at 60 FPS with dynamic amplitude autoscaling and live legends.
- **Configurable Sampling Frequencies**:
  - $25\text{ Hz}$ ($40\text{ ms}$) — Low power, macroscopic actions.
  - $50\text{ Hz}$ ($20\text{ ms}$) — Standard human activity baseline.
  - $100\text{ Hz}$ ($10\text{ ms}$) — High-resolution robotic manipulation capture.
- **Local Persistence via Room**: Batched background writes guarantee data preservation even if interrupted.
- **Interactive Dataset Review & Playback**:
  - Multi-axis inspection ($X$, $Y$, $Z$).
  - Scrubber tooltip reading exact values at any millisecond timestamp.
  - Animated playback sequencer with variable speed ($0.5\times$, $1\times$, $2\times$).
- **Storage Access Framework & Native Share**:
  - Native SAF document creation for direct file saving.
  - Android Share Sheet integration for instant transfer to Google Drive, Slack, AirDrop/Nearby Share, or email.

---

## 2. Technology Stack

| Component | Technology |
|---|---|
| Platform | Android (minSdk 26, targetSdk 34) |
| Language | Kotlin 1.9.24 |
| UI Toolkit | Jetpack Compose (Material 3 BOM 2024.05.00) |
| Architecture | Clean MVVM (Model-View-ViewModel) + Repository Pattern |
| Local Database | Room 2.6.1 (with KSP) |
| Sensor Access | Android `SensorManager` (`TYPE_ACCELEROMETER`, `TYPE_GYROSCOPE`, `TYPE_ROTATION_VECTOR`) |
| Concurrency | Kotlin Coroutines & `StateFlow` |
| File Export | Storage Access Framework (SAF) & `androidx.core.content.FileProvider` |

---

## 3. CSV Dataset Format

Every recorded demonstration is exported in RFC 4180 compliant CSV format, fully compatible with **Python pandas**, **NumPy**, **MATLAB**, **Excel**, and **Google Sheets**.

### Schema Header:
```csv
timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,rotation_x,rotation_y,rotation_z,rotation_w,label,session_id
```

### Field Definitions:
| Field | Units | Description |
|---|---|---|
| `timestamp` | seconds ($s$) | Monotonic relative time elapsed since recording start ($0.000, 0.010, \dots$) |
| `accel_x` | $m/s^2$ | Acceleration along smartphone $X$-axis (lateral right/left) |
| `accel_y` | $m/s^2$ | Acceleration along smartphone $Y$-axis (longitudinal up/down) |
| `accel_z` | $m/s^2$ | Acceleration along smartphone $Z$-axis (normal outward from screen) |
| `gyro_x` | $rad/s$ | Angular velocity around smartphone $X$-axis (pitch) |
| `gyro_y` | $rad/s$ | Angular velocity around smartphone $Y$-axis (roll) |
| `gyro_z` | $rad/s$ | Angular velocity around smartphone $Z$-axis (yaw) |
| `rotation_x` | unitless | Quaternion vector component $X$ |
| `rotation_y` | unitless | Quaternion vector component $Y$ |
| `rotation_z` | unitless | Quaternion vector component $Z$ |
| `rotation_w` | unitless | Quaternion scalar component $W$ (cos($\theta/2$)) |
| `label` | string | Action label identifier (e.g. `pick_object`, `place_object`) |
| `session_id` | string | Unique UUID assigned to the capture demonstration |

### Sample Output:
```csv
timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,rotation_x,rotation_y,rotation_z,rotation_w,label,session_id
0.000,0.1200,0.0500,9.7800,0.0100,0.0200,0.0000,0.0100,0.0200,0.0000,1.0000,pick_object,b78d2b38-2d88-4663-95c5-e11a3d348a54
0.010,0.1800,0.0900,9.6500,0.0400,0.0300,0.0100,0.0200,0.0300,0.0000,1.0000,pick_object,b78d2b38-2d88-4663-95c5-e11a3d348a54
0.020,0.3500,0.1400,9.4200,0.0800,0.0500,0.0200,0.0300,0.0400,0.0100,1.0000,pick_object,b78d2b38-2d88-4663-95c5-e11a3d348a54
```

### Python pandas Ingestion Example:
```python
import pandas as pd
import matplotlib.pyplot as plt

# Load MotionData CSV
df = pd.read_csv("Motion_pick_object_dataset.csv")

print(f"Dataset Loaded: {len(df)} samples")
print(f"Action: {df['label'].iloc[0]}")
print(f"Duration: {df['timestamp'].iloc[-1]:.2f} seconds")

# Plot 3-axis Accelerometer data
plt.figure(figsize=(10, 4))
plt.plot(df['timestamp'], df['accel_x'], label='Accel X (m/s²)', color='red')
plt.plot(df['timestamp'], df['accel_y'], label='Accel Y (m/s²)', color='green')
plt.plot(df['timestamp'], df['accel_z'], label='Accel Z (m/s²)', color='cyan')
plt.title("MotionData: 3-Axis Linear Acceleration Demonstration")
plt.xlabel("Time (seconds)")
plt.ylabel("Acceleration (m/s²)")
plt.grid(True, linestyle='--', alpha=0.5)
plt.legend()
plt.tight_layout()
plt.show()
```

---

## 4. Application Architecture

```
com.motiondata.app/
├── MotionDataApp.kt             # Application entrypoint (Room DB initialization)
├── MainActivity.kt              # Compose activity container & Edge-to-edge
├── data/
│   ├── model/
│   │   ├── SensorFrame.kt       # Synchronized multi-sensor reading snapshot
│   │   ├── MotionAction.kt      # Presets (Pick, Place, Rotate, Reach, Lift)
│   │   ├── SamplingFrequency.kt # 25Hz, 50Hz, 100Hz definitions
│   │   └── SensorAvailability.kt# Hardware sensor specs & diagnostics
│   ├── local/
│   │   ├── AppDatabase.kt       # Room Database configuration
│   │   ├── dao/
│   │   │   ├── SessionDao.kt    # Sessions queries, search, aggregate stats
│   │   │   └── SensorSampleDao.kt # Batch insertion & sample retrieval
│   │   └── entity/
│   │       ├── RecordingSessionEntity.kt # Session metadata entity
│   │       └── SensorSampleEntity.kt    # Individual sensor sample entity
│   └── repository/
│       ├── MotionRepository.kt  # Repository contract
│       └── MotionRepositoryImpl.kt # Room database repository implementation
├── sensor/
│   ├── MotionSensorManager.kt   # Background HandlerThread sensor recording engine
│   └── SensorCalibration.kt     # Zero-bias & noise variance calculator
├── export/
│   ├── CsvExporter.kt           # RFC 4180 CSV builder & SAF stream writer
│   └── MotionFileProvider.kt    # Android Content FileProvider for sharing
└── ui/
    ├── navigation/
    │   ├── Screen.kt            # Screen route declarations
    │   └── AppNavigation.kt     # NavHost and dependency wiring
    ├── theme/
    │   ├── Color.kt             # Deep Charcoal, CyanNeon, BlueElectric palette
    │   ├── Type.kt              # Monospace numbers & sans-serif typography
    │   └── Theme.kt             # Material 3 dark color scheme
    ├── components/
    │   ├── SensorRealtimeChart.kt # Real-time Canvas oscilloscope (X, Y, Z)
    │   ├── InteractiveHistoricalChart.kt # Scrubber & toggle historical Canvas
    │   ├── MetricCard.kt        # Technical telemetry indicator card
    │   └── RoboticsHeader.kt    # Top bar with status pill & navigation
    └── screens/
        ├── home/                # Screen 1: Home Dashboard & Stats
        ├── setup/               # Screen 2: Recording Setup & Wrist Guidelines
        ├── recording/           # Screen 3: Live Recording Oscilloscope & Stopwatch
        ├── summary/             # Screen 4: Quality Metrics & Data Preview Sheet
        ├── visualization/       # Screen 5: Interactive Graph & Playback Sequencer
        ├── history/             # Screen 6: Saved Datasets, Search, Rename, Export
        └── settings/            # Screen 7: Diagnostics & IMU Physics Notes
```

---

## 5. Physical Device Testing Protocol

To capture reliable demonstrations for robotics imitation learning:

1. **Strap the Phone Firmly**: Use an elastic sports forearm or wrist strap. Ensure the phone cannot wiggle or slide relative to the skin, as loose strapping introduces high-frequency mechanical vibration artifacts.
2. **Standardize Orientation**: Orient the phone so that the top edge points toward the hand/fingers. This ensures the phone’s $+Y$ coordinate aligns with the forearm extension axis.
3. **Run Sensor Calibration**: On the Setup screen, tap **Calibrate** while resting the phone on a flat table to confirm low noise variance ($\sigma < 0.15\text{ rad/s}$).
4. **Perform Demonstration**: Tap **Start Recording Session**, perform the manipulation action smoothly, then tap **Stop & Save**.
5. **Verify Data**: Review the live oscilloscope trace or the interactive playback screen to confirm continuous sensor signals with zero dropped frames.

---

## 6. Important Technical Limitations & IMU Physics

- **Acceleration vs 3D Position**: Accelerometers measure specific force including gravitational acceleration ($9.81\text{ m/s}^2$). Naively double-integrating linear acceleration into 3D position leads to rapid quadratic error drift within seconds without external visual SLAM or motion capture cameras.
- **Orientation vs Pose**: Gyroscopes provide angular velocity ($\text{rad/s}$). The Rotation Vector sensor combines accelerometer, gyroscope, and magnetometer data using Android's internal Kalman filter to produce orientation quaternions, but does not calculate absolute world translation.
- **Intended Use**: This application produces high-fidelity raw sensor time-series datasets to train imitation learning agents (such as diffusion policies or transformers) that map sensory state trajectories directly to robotic action spaces.
