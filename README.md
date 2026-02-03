# Disco Timer - Native Android App

This is a native Android implementation of the Disco Timer app using Kotlin and Jetpack Compose.

## Features

- ✅ Workout interval timer with work periods, cycles, and sets
- ✅ Persistent state storage using DataStore
- ✅ Wake lock to prevent screen sleep during timer
- ✅ Sound notifications (beep sound for countdown, finish sound when complete)
- ✅ Vibration feedback
- ✅ Mute/unmute functionality
- ✅ Pause/resume timer
- ✅ Gradient background matching the React Native version
- ✅ Full portrait orientation support
- ✅ Back button handling with confirmation dialog

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: StateFlow
- **Persistence**: DataStore Preferences
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Project Structure

```
app/src/main/java/com/anonymous/discotimer/
├── MainActivity.kt                          # Main entry point
├── data/
│   ├── TimerPreferences.kt                  # DataStore preferences management
│   └── TimerState.kt                        # Timer state data class
├── viewmodel/
│   └── TimerViewModel.kt                    # Main ViewModel with timer logic
├── ui/
│   ├── components/
│   │   ├── GradientBackground.kt            # Purple gradient background
│   │   ├── InputRow.kt                      # Reusable input component
│   │   ├── WorkTimer.kt                     # Large countdown display
│   │   ├── BottomTimer.kt                   # Cycles/Sets progress display
│   │   └── ScrollTimer.kt                   # Scrollable interval list
│   ├── screens/
│   │   ├── TimerFormScreen.kt               # Configuration screen
│   │   ├── TimerViewScreen.kt               # Active timer screen
│   │   └── TimerCompletedScreen.kt          # Completion screen
│   ├── navigation/
│   │   └── DiscoTimerNavigation.kt          # Navigation graph
│   └── theme/
│       ├── Color.kt                         # App colors
│       ├── Theme.kt                         # Material theme
│       └── Type.kt                          # Typography
└── utils/
    └── TimeFormatter.kt                     # Time formatting utilities

app/src/main/res/
├── raw/                                     # Sound files
│   ├── beep.wav
│   └── finish.mp3
└── values/
    ├── strings.xml                          # App strings
    └── themes.xml                           # App theme
```

## Building the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or later
- Android SDK with API level 34

### Build Instructions

1. **Open the project in Android Studio**
   ```bash
   cd /Users/loicnogues/Code/disco-timer/native-android
   open -a "Android Studio" .
   ```

2. **Sync Gradle**
   - Android Studio will automatically prompt you to sync Gradle
   - Or manually: File → Sync Project with Gradle Files

3. **Build the app**
   - Using Android Studio: Build → Make Project
   - Or via command line:
     ```bash
     ./gradlew build
     ```

4. **Run on device/emulator**
   - Click the "Run" button in Android Studio
   - Or via command line:
     ```bash
     ./gradlew installDebug
     ```

5. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   The APK will be located at: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Building a Signed Release APK

1. Create a keystore (if you don't have one):
   ```bash
   keytool -genkey -v -keystore disco-timer.keystore -alias discotimer -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in the project root:
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=discotimer
   storeFile=../disco-timer.keystore
   ```

3. Update `app/build.gradle.kts` to include signing config

4. Build signed release:
   ```bash
   ./gradlew assembleRelease
   ```

## Key Differences from React Native Version

### Similarities
- ✅ Same purple gradient background (#8E51FF to #E12AFB)
- ✅ Same timer logic and calculations
- ✅ Same UI layout and components
- ✅ Same sound effects and vibration
- ✅ Persistent state for work, cycles, sets, and mute settings
- ✅ Wake lock during timer
- ✅ Back button confirmation dialog

### Improvements
- ✅ Native Android performance
- ✅ Smaller APK size
- ✅ Better battery optimization
- ✅ Full Jetpack Compose modern UI
- ✅ Material Design 3 components
- ✅ Type-safe navigation

### Known Differences
- Uses Material 3 buttons instead of custom React Native buttons
- Default system font instead of SpaceMono
- Slightly different vibration pattern (native Android)

## Dependencies

```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.activity:activity-compose:1.8.2

// Jetpack Compose
androidx.compose:compose-bom:2024.02.00
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.navigation:navigation-compose:2.7.7

// ViewModel
androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0

// DataStore
androidx.datastore:datastore-preferences:1.0.0
```

## Usage

1. **Configure Timer**
   - Set work duration (in seconds, minimum 5, increments of 5)
   - Set number of cycles (minimum 1)
   - Set number of sets (minimum 1)
   - Toggle mute/unmute using the volume icon

2. **Start Timer**
   - Press "Start" button
   - Timer will keep screen awake during countdown
   - Large number shows current work interval countdown
   - Middle section shows upcoming intervals
   - Bottom section shows current cycle and set progress

3. **Control Timer**
   - Tap pause icon or the large countdown number to pause/resume
   - Toggle mute during timer
   - Press back button to exit (with confirmation)

4. **Completion**
   - Finish sound plays and device vibrates
   - Celebration screen appears
   - Press back to return to configuration

## Permissions

The app requires the following permissions:
- `WAKE_LOCK` - Keep screen on during timer
- `VIBRATE` - Vibration feedback

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## License

Same license as the original React Native Disco Timer app.

## Notes

- The app is portrait-only, matching the original design
- All state is persisted locally using DataStore
- Background timer continues even when app is backgrounded
- Sound files are embedded in the APK under `res/raw/`
