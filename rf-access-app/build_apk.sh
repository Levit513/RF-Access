#!/bin/bash

# RF Access APK Build Script - Direct Method
# This bypasses the Gradle wrapper compatibility issues

echo "Building RF Access APK using direct method..."

# Use system Android SDK tools directly
export ANDROID_HOME=/usr/local/android-sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0:$PATH

# Set Java 11 for compatibility
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Clean previous builds
rm -rf app/build/

# Create build directories
mkdir -p app/build/outputs/apk/debug/

# Compile resources
echo "Compiling resources..."
aapt2 compile --dir app/src/main/res -o app/build/compiled_resources.zip

# Link resources and generate R.java
echo "Linking resources..."
aapt2 link --proto-format -o app/build/app.apk \
    -I $ANDROID_HOME/platforms/android-34/android.jar \
    --manifest app/src/main/AndroidManifest.xml \
    app/build/compiled_resources.zip \
    --java app/build/gen

# Compile Java sources
echo "Compiling Java sources..."
mkdir -p app/build/classes
javac -d app/build/classes \
    -cp "$ANDROID_HOME/platforms/android-34/android.jar:$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar" \
    app/src/main/java/com/solutions513/rfaccess/*.java \
    app/build/gen/com/solutions513/rfaccess/R.java

# Convert to DEX
echo "Converting to DEX..."
d8 --lib $ANDROID_HOME/platforms/android-34/android.jar \
    --output app/build/classes.dex \
    app/build/classes/com/solutions513/rfaccess/*.class

# Add DEX to APK
echo "Adding DEX to APK..."
aapt add app/build/app.apk app/build/classes.dex

# Sign APK (debug key)
echo "Signing APK..."
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore ~/.android/debug.keystore \
    -storepass android \
    -keypass android \
    app/build/app.apk androiddebugkey

# Align APK
echo "Aligning APK..."
zipalign -v 4 app/build/app.apk app/build/outputs/apk/debug/app-debug.apk

echo "APK built successfully: app/build/outputs/apk/debug/app-debug.apk"
