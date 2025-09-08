# RF Access Deep Linking Guide

## 🔗 **Deep Linking URLs for Distribution**

### **1. Custom Scheme URLs (Recommended)**
These URLs will **force the app to open** without showing a webpage:

```
rfaccess://open?username=john_doe&cardData=1234567890ABCDEF&action=program
```

**Parameters:**
- `username`: Target user's username
- `cardData`: Hex-encoded MIFARE card data
- `action`: Action to perform (`program` or `emulate`)

### **2. HTTPS URLs with Specific Paths**
These URLs use specific paths to force app opening:

```
https://rfaccess.app/app?username=john_doe&cardData=1234567890ABCDEF&action=program
https://app.rfaccess.com/open?username=john_doe&cardData=1234567890ABCDEF&action=program
```

## 📱 **How to Test Deep Links**

### **Method 1: ADB Command (Recommended)**
```bash
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "rfaccess://open?username=testuser&cardData=1234567890ABCDEF&action=program" \
  com.example.rfaccess
```

### **Method 2: Browser Test**
1. Open browser on Android device
2. Type the URL in address bar
3. The app should open directly (not show webpage)

### **Method 3: QR Code**
Generate QR codes with the deep link URLs for easy distribution.

## 🛠 **Why Deep Links Were Opening Webpages**

**Problem:** Android was defaulting to browser because:
1. No domain verification for HTTPS links
2. Low intent filter priority
3. Generic path matching

**Solution Applied:**
1. ✅ Added `android:priority="1000"` to intent filters
2. ✅ Used specific paths (`/app`, `/open`) instead of root domains
3. ✅ Custom scheme `rfaccess://` for guaranteed app opening

## 📋 **Distribution Workflow**

### **For Admins:**
1. Login to RF Access app
2. Go to Admin Portal → MIFARE Emulation Control
3. Enter target username and card data
4. Send deep link via:
   - SMS: `rfaccess://open?username=USER&cardData=DATA&action=program`
   - Email with QR code
   - Push notification (automatic)

### **For Users:**
1. Receive deep link (SMS/email/notification)
2. Tap link → RF Access app opens automatically
3. Card programming data is loaded
4. Hold NFC card to phone to program

## 🎯 **MIFARE Card Emulation**

### **Remote Emulation Control:**
```
rfaccess://open?username=USER&cardData=HEXDATA&action=emulate
```

This will:
1. Open the app
2. Load card data into emulation service
3. Activate Host Card Emulation (HCE)
4. Phone acts as MIFARE card for readers

### **Emulation Commands via Firebase:**
Admins can send remote emulation commands through the admin portal that will:
- Update target user's emulation data
- Activate/deactivate card emulation
- Send push notifications

## 🔧 **Technical Implementation**

### **Intent Filters (AndroidManifest.xml):**
```xml
<!-- High priority custom scheme -->
<intent-filter android:priority="1000">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="rfaccess" />
</intent-filter>

<!-- Specific HTTPS paths -->
<intent-filter android:priority="1000">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="https"
          android:host="rfaccess.app"
          android:pathPrefix="/app" />
</intent-filter>
```

### **Host Card Emulation Service:**
- **Service:** `MifareEmulationService.java`
- **Configuration:** `/res/xml/apdu_service.xml`
- **AIDs:** Custom RF Access identifiers
- **Remote Control:** Via Firebase Cloud Messaging

## 🚀 **Next Steps**

1. **Test the deep links** using ADB commands
2. **Verify app opens directly** (no webpage)
3. **Test MIFARE emulation** with NFC readers
4. **Deploy to users** with proper deep link URLs

The app now supports both **card programming** and **card emulation** with full remote control capabilities!
