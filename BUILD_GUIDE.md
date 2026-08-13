# Quick Build Guide for Disco Timer Native Android

## Prerequisites

Before building, ensure you have:
- ✅ Android Studio installed
- ✅ JDK 8 or higher
- ✅ Android SDK with API 34

## Option 1: Build with Android Studio (Recommended)

1. **Open the project**
   ```bash
   cd /Users/loicnogues/Code/disco-timer/native-android
   ```
   Then open this folder in Android Studio

2. **Let Android Studio sync**
   - Wait for Gradle sync to complete
   - Android Studio will download all dependencies automatically

3. **Generate launcher icons** (Important!)
   - Right-click on `res` folder → New → Image Asset
   - Choose "Launcher Icons (Adaptive and Legacy)"
   - Upload a disco ball icon or use a simple colored square
   - Click "Next" then "Finish"

4. **Build and run**
   - Click the green "Run" button
   - Select your device/emulator
   - App will install and launch

## Option 2: Command Line Build

1. **First time setup**
   
   You need to add launcher icons manually or the build will fail. Create placeholder icons:
   
   ```bash
   cd native-android
   
   # Copy any PNG as placeholder (or use ImageMagick to create colored squares)
   # You need ic_launcher.png in these sizes:
   # - res/mipmap-mdpi/ic_launcher.png (48x48)
   # - res/mipmap-hdpi/ic_launcher.png (72x72)
   # - res/mipmap-xhdpi/ic_launcher.png (96x96)
   # - res/mipmap-xxhdpi/ic_launcher.png (144x144)
   # - res/mipmap-xxxhdpi/ic_launcher.png (192x192)
   ```

2. **Build debug APK**
   ```bash
   cd /Users/loicnogues/Code/disco-timer/native-android
   ./gradlew assembleDebug
   ```
   
   APK location: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## Option 3: Build Release APK

### Without Signing (For testing)
```bash
./gradlew assembleRelease
```
APK location: `app/build/outputs/apk/release/app-release-unsigned.apk`

### With Signing (For distribution)

1. **Create keystore** (one-time setup)
   ```bash
   cd /Users/loicnogues/Code/disco-timer/native-android
   keytool -genkey -v -keystore disco-timer.keystore \
     -alias discotimer -keyalg RSA -keysize 2048 -validity 10000
   ```
   
   Remember the passwords you set!

2. **Create keystore.properties**
   Create a file `keystore.properties` in the project root (it is gitignored):
   ```properties
   storeFile=disco-timer.keystore
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=discotimer
   keyPassword=YOUR_KEY_PASSWORD
   ```

   The build script (`app/build.gradle.kts`) picks this file up automatically.
   Alternatively, set the `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
   and `KEY_PASSWORD` environment variables (this is what CI uses).

3. **Build signed release**
   ```bash
   ./gradlew assembleRelease
   ```
   APK location: `app/build/outputs/apk/release/app-release.apk`

## Publishing a Release (GitHub / Obtainium)

Releases are built and published automatically by `.github/workflows/release.yml`.

1. **One-time setup**: add these repository secrets on GitHub
   (Settings → Secrets and variables → Actions):
   - `KEYSTORE_BASE64` — the keystore file, base64-encoded:
     `base64 -i disco-timer.keystore | pbcopy`
   - `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — matching the keystore

2. **Release**: bump `versionCode` and `versionName` in `app/build.gradle.kts`,
   commit, then tag and push:
   ```bash
   git tag v0.2.2
   git push origin master v0.2.2
   ```

   The workflow builds a signed APK and attaches it to a GitHub release, which
   Obtainium picks up automatically.

   ⚠️ Back up the keystore somewhere safe. All future releases must be signed
   with the same key, or users will have to uninstall/reinstall the app.

## Troubleshooting

### "gradlew: command not found"
The gradlew script might not be executable:
```bash
chmod +x gradlew
```

### "SDK location not found"
Create `local.properties`:
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
# Or on macOS typically:
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

### Missing launcher icons
You MUST have launcher icons. Use Android Studio's Image Asset tool (recommended) or manually create PNG files in the required mipmap folders.

### Gradle sync fails
Try:
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

## Testing on Device

### Via USB (ADB)
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect via USB
4. Run:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Via APK file
1. Build the APK (debug or release)
2. Transfer to your device
3. Open and install (you may need to enable "Install from Unknown Sources")

## Next Steps After Building

Once built successfully:
- ✅ Test all timer functionality
- ✅ Test pause/resume
- ✅ Test mute/unmute
- ✅ Test back button handling
- ✅ Test completion screen
- ✅ Test state persistence (close app and reopen)
- ✅ Test background timer (home button during countdown)

## File Sizes (Approximate)

- Debug APK: ~8-10 MB
- Release APK (unsigned): ~5-7 MB
- Release APK (signed & optimized): ~5-7 MB

Much smaller than the React Native APK which is typically 30-50 MB!
