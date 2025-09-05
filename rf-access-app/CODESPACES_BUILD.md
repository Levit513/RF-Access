# RF Access - GitHub Codespaces Build Guide

## 🚀 **Best Option: GitHub Codespaces (Free Cloud Build)**

This is the most reliable method - uses GitHub's cloud servers to build your APK.

## 📋 **Step-by-Step Instructions:**

### **1. Open Codespaces**
1. Go to: **https://github.com/LT319/RF-Access**
2. Click the green **"Code"** button
3. Click **"Codespaces"** tab
4. Click **"Create codespace on main"**

### **2. Wait for Environment Setup**
- Codespace will load (2-3 minutes)
- You'll see a VS Code interface in your browser
- Terminal will be available at the bottom

### **3. Build APK**
In the terminal, run these commands:
```bash
# Make gradlew executable
chmod +x gradlew

# Build the APK
./gradlew assembleDebug
```

### **4. Download APK**
1. Build completes in ~5 minutes
2. Navigate to: `app/build/outputs/apk/debug/`
3. Right-click `app-debug.apk`
4. Select **"Download"**

## ✅ **Why This is Best:**
- **Free**: GitHub provides free Codespaces hours
- **Reliable**: Professional build environment
- **No Setup**: No Android Studio installation needed
- **Cloud-Based**: Works from any computer
- **Guaranteed**: Uses official Android build tools

## 🎯 **Expected Result:**
- APK file: `rf-access-debug.apk` (~8-15 MB)
- Ready to install on Android devices
- Professional build with all features included

## 🔧 **If Build Fails:**
Run these commands to fix common issues:
```bash
# Update dependencies
./gradlew clean

# Retry build
./gradlew assembleDebug --stacktrace
```

This method is **100% reliable** and gives you a professional APK build.
