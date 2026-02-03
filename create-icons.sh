#!/bin/bash

# Create simple colored PNG icons using ImageMagick (if available) or copy from existing project
if command -v convert &> /dev/null; then
    echo "Creating launcher icons with ImageMagick..."
    convert -size 48x48 xc:"#8E51FF" app/src/main/res/mipmap-mdpi/ic_launcher.png
    convert -size 72x72 xc:"#8E51FF" app/src/main/res/mipmap-hdpi/ic_launcher.png
    convert -size 96x96 xc:"#8E51FF" app/src/main/res/mipmap-xhdpi/ic_launcher.png
    convert -size 144x144 xc:"#8E51FF" app/src/main/res/mipmap-xxhdpi/ic_launcher.png
    convert -size 192x192 xc:"#8E51FF" app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
    
    convert -size 48x48 xc:"#8E51FF" app/src/main/res/mipmap-mdpi/ic_launcher_round.png
    convert -size 72x72 xc:"#8E51FF" app/src/main/res/mipmap-hdpi/ic_launcher_round.png
    convert -size 96x96 xc:"#8E51FF" app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
    convert -size 144x144 xc:"#8E51FF" app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
    convert -size 192x192 xc:"#8E51FF" app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png
    echo "✅ Icons created!"
else
    echo "ImageMagick not found. Trying to copy from React Native project..."
    if [ -f "../android/app/src/main/res/mipmap-hdpi/ic_launcher.png" ]; then
        cp -r ../android/app/src/main/res/mipmap-* app/src/main/res/
        echo "✅ Icons copied from React Native project!"
    else
        echo "❌ Cannot create icons. Please use Android Studio: Right-click res → New → Image Asset"
    fi
fi
