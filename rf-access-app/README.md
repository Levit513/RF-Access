# RF Access - Digital Access Cards

Professional Android app for simplified MIFARE Classic card programming with admin control.

## 🚀 **Quick Start - Get Your APK**

### **Automatic Build (Recommended)**
1. **Fork this repository** on GitHub
2. **Push to your GitHub** - APK builds automatically
3. **Download APK** from GitHub Actions or Releases
4. **Install on Android** device

### **Manual Build**
```bash
git clone https://github.com/yourusername/rf-access.git
cd rf-access
./gradlew assembleDebug
```

## 📱 **RF Access Consumer App**

### **User Experience**
- **Simple Interface**: "RF Access Programming"
- **Clear Instructions**: "Hold card to phone to program your RF Access Card"
- **Zero Technical Details**: No MIFARE/sector visibility
- **Push Notifications**: Automatic alerts for new programs
- **One-Tap Programming**: Seamless card programming

### **User Workflow**
1. Download RF Access from Play Store (or install APK)
2. Sign up with username
3. Receive notification: "You have a new access card ready to program"
4. Tap "Program My Card"
5. Hold card to phone
6. Success: "Your RF Access Card has been programmed successfully!"

## 🔧 **Admin Portal**

### **Admin-Only Features**
- **User Management**: Add/manage end users by username
- **Program Creation**: Full MIFARE sector editor with technical control
- **Push Distribution**: Send programs to specific users instantly
- **Usage Tracking**: Monitor who programmed what and when
- **Access Control**: Admin interface only accessible to authorized users

### **Admin Workflow**
1. Create MIFARE programs using sector editor
2. Add users by username
3. Push programs to specific users
4. Users receive automatic notifications
5. Track programming completion in real-time

## 🏗️ **Architecture**

### **Two-Tier System**
- **Consumer App**: User-friendly, secure, no technical data exposed
- **Admin Portal**: Full MIFARE control, user management, program distribution

### **Security Features**
- **Firebase Authentication**: Secure user login
- **Token-Based Access**: One-time programming tokens
- **Encrypted Communication**: Secure data transmission
- **Role-Based Access**: Admin vs. end-user permissions

## 🔐 **Data Flow**

1. **Admin creates** MIFARE programs in admin portal
2. **Admin pushes** programs to users via Firebase notifications
3. **Users receive** "RF Access Programming" notification
4. **App downloads** encrypted program data securely
5. **Native NFC APIs** program card with raw MIFARE data
6. **System reports** completion back to admin portal

## 📋 **Requirements**

### **Android App**
- Android 7.0+ (API 24+)
- NFC-enabled device
- Internet connection for program downloads

### **Admin Portal**
- Python 3.8+
- Flask web framework
- Firebase Admin SDK
- SQLite database

## 🏪 **Play Store Publishing**

### **Ready for Submission**
- ✅ App Bundle (.aab) format ready
- ✅ Privacy Policy included
- ✅ Store listing content prepared
- ✅ Screenshots and graphics planned
- ✅ Content rating: Everyone
- ✅ Permissions: NFC, Internet, Notifications

### **Publishing Steps**
1. Create Google Play Developer account ($25)
2. Upload APK/AAB to Play Console
3. Add store listing (name, description, screenshots)
4. Submit for review (1-3 days)
5. Publish to Play Store

## 🛠️ **Development Setup**

### **Android App**
```bash
# Clone repository
git clone https://github.com/yourusername/rf-access.git
cd rf-access

# Build APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### **Admin Portal**
```bash
# Setup admin portal
cd admin-portal
pip install -r requirements.txt

# Configure Firebase
# Add firebase-service-account.json

# Run admin portal
python app.py
```

## 📖 **Documentation**

- **[Play Store Guide](PLAY_STORE_GUIDE.md)**: Complete publishing instructions
- **[Setup Guide](SETUP_GUIDE.md)**: Development environment setup
- **[Build Instructions](BUILD_INSTRUCTIONS.md)**: Detailed build process
- **[Direct Install Guide](DIRECT_INSTALL_GUIDE.md)**: Alternative installation methods

## 🎯 **Features**

### **Consumer App Features**
- Simple "RF Access Programming" interface
- Push notifications for new programs
- One-tap card programming
- No technical MIFARE details visible
- Secure token-based access
- Firebase authentication

### **Admin Portal Features**
- Full MIFARE sector editor
- User management by username
- Program creation and distribution
- Push notification triggers
- Usage tracking and analytics
- Role-based access control

## 🔧 **Technical Stack**

### **Android App**
- **Language**: Kotlin
- **Framework**: Android Native
- **NFC**: Android NFC APIs (MifareClassic)
- **Authentication**: Firebase Auth
- **Notifications**: Firebase Cloud Messaging
- **Networking**: Retrofit + OkHttp
- **UI**: Material Design Components

### **Admin Portal**
- **Backend**: Flask (Python)
- **Database**: SQLite
- **Authentication**: Firebase Admin SDK
- **Notifications**: Firebase Cloud Messaging
- **Frontend**: Bootstrap + JavaScript

## 📞 **Support**

For technical support or questions:
- Create an issue in this repository
- Contact: support@513solutions.com

## 📄 **License**

Copyright © 2024 513 Solutions. All rights reserved.

---

**RF Access** - Professional digital access card management made simple.
