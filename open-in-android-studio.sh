#!/bin/bash

echo "🪩 Opening Disco Timer Native Android in Android Studio..."

# Check if Android Studio is installed
if [ -d "/Applications/Android Studio.app" ]; then
    open -a "Android Studio" .
    echo "✅ Android Studio launched!"
    echo ""
    echo "Next steps:"
    echo "1. Wait for Gradle sync to complete"
    echo "2. Generate launcher icons: Right-click 'res' → New → Image Asset"
    echo "3. Click the green 'Run' button"
else
    echo "❌ Android Studio not found at /Applications/Android Studio.app"
    echo "Please install Android Studio or update the path in this script."
fi
