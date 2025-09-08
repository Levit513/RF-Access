# Deep Linking Test Guide for RF Access

## 🔗 **Problem Analysis**
Your link `https://programmer.513solutions.com/program/46HC8qr3CWeCvHILi1vadZK8-abcsj9x4wH4j0eUnKk` opens Chrome because:

1. **Missing Domain Verification** - Android doesn't know your app handles this domain
2. **No Digital Asset Links** - The domain needs to verify it trusts your app
3. **Intent Filter Missing** - App wasn't configured for `programmer.513solutions.com`

## ✅ **Fixes Applied**

### **1. Added Intent Filter for Your Domain**
```xml
<intent-filter android:autoVerify="true" android:priority="1000">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="https"
          android:host="programmer.513solutions.com"
          android:pathPrefix="/program" />
</intent-filter>
```

### **2. Created Digital Asset Links File**
File: `digital-asset-links.json` (needs to be hosted at your domain)

### **3. Updated LoginActivity**
Now handles `programmer.513solutions.com` URLs and extracts the token from the path.

## 🧪 **Testing Methods**

### **Method 1: ADB Command Testing (Recommended)**
```bash
# Test your actual link
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "https://programmer.513solutions.com/program/46HC8qr3CWeCvHILi1vadZK8-abcsj9x4wH4j0eUnKk" \
  com.example.rfaccess

# Test custom scheme (should work immediately)
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "rfaccess://open?username=testuser&cardData=46HC8qr3CWeCvHILi1vadZK8-abcsj9x4wH4j0eUnKk&action=program" \
  com.example.rfaccess
```

### **Method 2: Browser Test**
1. Install the updated APK on your device
2. Open Chrome and paste: `https://programmer.513solutions.com/program/46HC8qr3CWeCvHILi1vadZK8-abcsj9x4wH4j0eUnKk`
3. Should show "Open with RF Access" option

### **Method 3: Intent Verification**
```bash
# Check if Android recognizes your app for the domain
adb shell dumpsys package domain-preferred-apps
```

## 🌐 **Domain Setup Required**

### **Critical Step: Host Digital Asset Links**
You need to upload `digital-asset-links.json` to:
```
https://programmer.513solutions.com/.well-known/assetlinks.json
```

**File Contents:**
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example.rfaccess",
    "sha256_cert_fingerprints":
    ["14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"]
  }
}]
```

### **Get Your App's Certificate Fingerprint**
```bash
# For debug builds
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# For release builds
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

## 🔧 **Immediate Workaround**

While setting up domain verification, use custom scheme URLs:
```
rfaccess://open?username=USER&cardData=46HC8qr3CWeCvHILi1vadZK8-abcsj9x4wH4j0eUnKk&action=program
```

## 📋 **Step-by-Step Fix Process**

1. **Build & Install Updated APK** ✅ (Done - includes new intent filter)
2. **Host Digital Asset Links** ⏳ (You need to do this on your server)
3. **Test with ADB** ⏳ (Use commands above)
4. **Verify Domain Association** ⏳ (Check with dumpsys command)
5. **Test in Browser** ⏳ (Should work after steps 1-2)

## 🎯 **Expected Results**

After completing all steps:
- ✅ `rfaccess://` links open app immediately
- ✅ `https://programmer.513solutions.com/program/...` links open app directly
- ✅ No browser redirect
- ✅ Programming data extracted and processed

The key missing piece is hosting the digital asset links file on your domain!
