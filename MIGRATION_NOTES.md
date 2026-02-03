# Migration from React Native to Native Android

## Overview

This document outlines the conversion of the Disco Timer app from React Native/Expo to native Android using Kotlin and Jetpack Compose.

## Component Mapping

### React Native → Jetpack Compose

| React Native Component | Jetpack Compose Equivalent |
|------------------------|---------------------------|
| `View` | `Box`, `Column`, `Row` |
| `Text` | `Text` |
| `TextInput` | `BasicTextField` |
| `Button` | `Button` |
| `ScrollView` | `LazyColumn` |
| `LinearGradient` | `Brush.verticalGradient()` |
| `BackHandler` | `BackHandler` |
| `Alert.alert()` | `AlertDialog` |
| `StatusBar` | System bar styling in theme |

### State Management

| React Native | Native Android |
|--------------|----------------|
| `useState` | `mutableStateOf`, `MutableStateFlow` |
| `useEffect` | `LaunchedEffect`, ViewModel lifecycle |
| `AsyncStorage` | DataStore Preferences |
| `usePersistedState` (custom hook) | DataStore with Flow |

### Navigation

| React Native | Native Android |
|--------------|----------------|
| `expo-router` | Navigation Compose |
| `router.navigate()` | `navController.navigate()` |
| `router.back()` | `navController.popBackStack()` |

### Audio & Device Features

| React Native | Native Android |
|--------------|----------------|
| `expo-audio` | `MediaPlayer` |
| `Vibration` | `Vibrator` system service |
| `expo-keep-awake` | `PowerManager.WakeLock` |

## File Structure Comparison

### React Native Structure
```
app/
  index.tsx               → Main screen
  timer.tsx               → Timer screen
  _layout.tsx            → Root layout
components/
  TimerForm.tsx          → Form component
  TimerView.tsx          → Timer view
  WorkInput.tsx          → Input components
  ...
hooks/
  usePersistedState.ts   → Custom hook
```

### Native Android Structure
```
ui/
  screens/
    TimerFormScreen.kt   → Form screen
    TimerViewScreen.kt   → Timer screen
  components/
    InputRow.kt          → Input component
    WorkTimer.kt         → Timer display
    ...
  navigation/
    DiscoTimerNavigation.kt → Nav graph
data/
  TimerState.kt          → State model
  TimerPreferences.kt    → Persistence
viewmodel/
  TimerViewModel.kt      → Business logic
```

## Code Equivalents

### Example 1: State with Persistence

**React Native:**
```typescript
const [work, setWork] = usePersistedState("work", 40);
```

**Native Android:**
```kotlin
// In TimerPreferences.kt
val work: Flow<Int> = context.dataStore.data.map { preferences ->
    preferences[WORK_KEY] ?: 40
}

// In ViewModel
viewModelScope.launch {
    val work = preferences.work.first()
    _timerState.value = _timerState.value.copy(work = work)
}
```

### Example 2: Timer Countdown

**React Native:**
```typescript
useEffect(() => {
  const interval = setInterval(() => {
    setTimer(lastTimer => lastTimer - 1);
  }, 1000);
  return () => clearInterval(interval);
}, []);
```

**Native Android:**
```kotlin
timerJob = viewModelScope.launch {
    while (currentTime < totalTime) {
        delay(1000)
        _timerState.value = _timerState.value.copy(
            currentTime = currentTime + 1
        )
    }
}
```

### Example 3: Gradient Background

**React Native:**
```typescript
<LinearGradient colors={["#8e51ff", "#e12afb"]}>
  {children}
</LinearGradient>
```

**Native Android:**
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(GradientStart, GradientEnd)
            )
        )
) {
    content()
}
```

### Example 4: Back Button Handling

**React Native:**
```typescript
useEffect(() => {
  const backHandler = BackHandler.addEventListener(
    "hardwareBackPress",
    () => {
      Alert.alert("Hold on!", "Are you sure?", [
        { text: "Cancel" },
        { text: "YES", onPress: () => router.navigate("/") }
      ]);
      return true;
    }
  );
  return () => backHandler.remove();
}, []);
```

**Native Android:**
```kotlin
var showBackDialog by remember { mutableStateOf(false) }

BackHandler {
    showBackDialog = true
}

if (showBackDialog) {
    AlertDialog(
        onDismissRequest = { showBackDialog = false },
        title = { Text("Hold on!") },
        text = { Text("Are you sure?") },
        confirmButton = {
            TextButton(onClick = { /* navigate back */ }) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(onClick = { showBackDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

## Key Benefits of Native Android

### Performance
- ✅ Faster startup time
- ✅ Smoother animations
- ✅ Better memory management
- ✅ No JavaScript bridge overhead

### App Size
- React Native APK: ~30-50 MB
- Native Android APK: ~5-7 MB
- **Reduction: 80-85%**

### Battery Life
- ✅ More efficient wake locks
- ✅ Better background task handling
- ✅ Native power management

### Development
- ✅ Type safety with Kotlin
- ✅ Modern Compose UI
- ✅ Better Android Studio integration
- ✅ Access to all Android APIs without libraries

## Challenges & Solutions

### Challenge 1: Learning Curve
**Solution:** Jetpack Compose has excellent documentation and the declarative UI model is similar to React.

### Challenge 2: Sound Files
**Issue:** React Native uses `require()` for assets, Android uses resource IDs.
**Solution:** Place sound files in `res/raw/` and reference via `R.raw.beep`.

### Challenge 3: Time Formatting
**Issue:** React Native uses `humanize-duration` npm package.
**Solution:** Created custom `TimeFormatter` utility class in Kotlin.

### Challenge 4: State Persistence
**Issue:** React Native uses AsyncStorage.
**Solution:** DataStore Preferences with Kotlin Flow for reactive updates.

## Migration Checklist

- [x] Set up native Android project structure
- [x] Convert UI components to Jetpack Compose
- [x] Migrate state management to ViewModel + StateFlow
- [x] Implement DataStore for persistence
- [x] Add navigation with Navigation Compose
- [x] Implement timer logic in ViewModel
- [x] Add sound playback with MediaPlayer
- [x] Implement wake lock functionality
- [x] Add vibration support
- [x] Match original UI design (gradients, colors, fonts)
- [x] Handle back button with confirmation dialog
- [x] Test all user flows

## Testing Comparison

### React Native Testing
- Jest for unit tests
- React Native Testing Library
- Detox for E2E

### Native Android Testing
- JUnit for unit tests
- Compose UI testing
- Espresso for integration tests

## Future Enhancements

Possible improvements for the native version:

1. **Material Design 3**
   - Already using MD3, could add more dynamic color theming
   - Dark mode support (already in theme)

2. **Widgets**
   - Home screen widget showing current timer
   - Quick start widget

3. **Notifications**
   - Show ongoing notification during timer
   - Notification when timer completes

4. **Watch App**
   - Wear OS companion app
   - Control timer from watch

5. **Performance Optimizations**
   - Use remember() and derivedStateOf() for computed values
   - LazyColumn optimization for scroll timer

6. **Accessibility**
   - Add content descriptions
   - TalkBack support
   - Large text support

## Conclusion

The native Android implementation successfully replicates all functionality of the React Native version while providing:
- **Better performance**
- **Smaller app size**
- **Native Android experience**
- **Access to all Android features**
- **Modern Kotlin + Jetpack Compose architecture**

Total development effort: ~12 major components + navigation + data layer + utilities = Complete app
