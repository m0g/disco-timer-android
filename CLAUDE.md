# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Disco Timer is a native Android workout interval timer written in Kotlin with Jetpack Compose. It was converted from a React Native/Expo app for better performance and smaller APK size (5-7MB vs 30-50MB).

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean and refresh
./gradlew clean
./gradlew --refresh-dependencies
```

APK outputs are in `app/build/outputs/apk/`.

## Architecture

The app follows MVVM with a single-module structure:

```
app/src/main/java/com/anonymous/discotimer/
├── MainActivity.kt              # Entry point
├── data/
│   ├── TimerState.kt           # State data class with computed properties
│   └── TimerPreferences.kt     # DataStore persistence
├── viewmodel/
│   └── TimerViewModel.kt       # Business logic, timer countdown, audio, vibration
├── ui/
│   ├── components/             # Reusable composables (InputRow, WorkTimer, etc.)
│   ├── screens/                # TimerFormScreen, TimerViewScreen, TimerCompletedScreen
│   ├── navigation/             # Navigation graph with sealed class routes
│   └── theme/                  # Colors, Theme, Typography
└── utils/
    └── TimeFormatter.kt        # Time formatting utilities
```

### State Flow

Single source of truth: `TimerViewModel.timerState` (StateFlow). UI collects via `.collectAsState()`. Timer logic runs in a coroutine loop with `delay(1000)` for each tick.

### Key Implementation Details

- **Persistence**: DataStore Preferences (not SharedPreferences)
- **Audio**: MediaPlayer with `R.raw.beep` (countdown) and `R.raw.finish` (completion)
- **Wake Lock**: `SCREEN_BRIGHT_WAKE_LOCK` with 10-hour timeout, released in `onCleared()`
- **Vibration**: 200ms pulses on countdown beeps and completion
- **Pause**: Timer loop checks `isPaused` flag, not job cancellation
- **Navigation**: Two screens (`TimerForm`, `TimerView`) with shared ViewModel instance

### TimerState Computed Properties

`currentWorkTime`, `currentCycle`, `currentSet`, `totalTime`, `remainingTime` are all computed via `get()` from the base state values (`work`, `cycles`, `sets`, `currentTime`).

## Build Configuration

- Min SDK: 24 (Android 7.0)
- Target/Compile SDK: 34 (Android 14)
- Kotlin: 1.9.20
- Compose Compiler: 1.5.4
- Compose BOM: 2024.02.00

## Permissions

- `WAKE_LOCK` - Keep screen on during timer
- `VIBRATE` - Feedback on countdown/completion

## UI Conventions

- Purple gradient background (`#8E51FF` to `#E12AFB`)
- Cyan highlight for current interval (`#53EAFD`)
- Portrait orientation locked
- Uses `BasicTextField` (not Material `TextField`)
- All strings in `res/values/strings.xml`
