# RF Access - MIFARE Card Programming System

A complete NFC card programming solution with Android app and web-based admin portal.

## 🚀 Features

### Android App
- **User-friendly interface** - No technical details visible to end users
- **NFC card programming** - Support for MIFARE Classic cards
- **Real-time status updates** - Clear feedback during programming process
- **Admin portal integration** - Fetches programming data automatically
- **Error handling** - Comprehensive validation and user feedback

### Admin Portal
- **Web-based interface** - Simple form for setting MIFARE programming data
- **JSON configuration** - Flexible sector and block data management
- **Railway deployment** - Production-ready cloud hosting
- **RESTful API** - `/program` endpoint for Android app integration

## 📱 Quick Start

### For End Users
1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from unknown sources" on your Android device
3. Install the RF Access app
4. Grant NFC permissions when prompted
5. Open the app and tap "Program Card"
6. Hold your MIFARE card to the back of your phone

### For Administrators
1. Access the admin portal: https://rf-access-admin-production.up.railway.app
2. Enter MIFARE programming data in JSON format
3. Click "Set Programming Data"
4. Users can now program cards with the configured data

## 🏗️ Architecture

```
┌─────────────────┐    HTTP     ┌──────────────────┐
│   Android App   │ ◄─────────► │   Admin Portal   │
│   (RF Access)   │             │   (Railway)      │
└─────────────────┘             └──────────────────┘
         │                               │
         │ NFC                          │ Web Interface
         ▼                               ▼
┌─────────────────┐             ┌──────────────────┐
│  MIFARE Cards   │             │  Administrator   │
└─────────────────┘             └──────────────────┘
```

## 🔧 Development

### Building the Android App

The project uses GitHub Actions for automated builds:

1. **Automatic builds** trigger on every push to main branch
2. **APK artifacts** are uploaded to GitHub Actions
3. **Releases** are created automatically with version tags

#### Manual Build (Local)
```bash
cd rf-access-app
./gradlew assembleDebug
```

#### Build Requirements
- Java 11+
- Android SDK API 32
- Gradle 7.4

### Admin Portal Development

The admin portal is a Flask application deployed on Railway:

```bash
cd admin-portal
pip install -r requirements.txt
python app.py
```

## 📋 MIFARE Programming Data Format

The admin portal expects JSON data in this format:

```json
{
  "sectors": {
    "1": {
      "0": "01020304050607080910111213141516",
      "1": "17181920212223242526272829303132",
      "2": "33343536373839404142434445464748"
    },
    "2": {
      "0": "49505152535455565758596061626364",
      "1": "65666768697071727374757677787980"
    }
  }
}
```

- **sectors**: Object containing sector numbers as keys
- **blocks**: Each sector contains block numbers (0, 1, 2) as keys
- **data**: 32-character hex strings (16 bytes) for each block

## 🔒 Security Notes

- Default MIFARE keys are used for authentication
- Trailer blocks (sector 3) are automatically skipped
- Invalid hex data is rejected with error messages
- NFC permissions are required for card programming

## 🚀 Deployment

### Android App
- Automated builds via GitHub Actions
- APK releases published to GitHub
- Ready for Google Play Store submission

### Admin Portal
- Deployed on Railway: https://rf-access-admin-production.up.railway.app
- Environment variables configured for production
- Automatic deployments from main branch

## 📖 API Documentation

### GET /program
Returns the current MIFARE programming configuration.

**Response:**
```json
{
  "sectors": {
    "1": {
      "0": "hex_data_32_chars",
      "1": "hex_data_32_chars"
    }
  }
}
```

**Status Codes:**
- `200`: Programming data available
- `404`: No programming data configured

## 🛠️ Troubleshooting

### Android App Issues
- **NFC not working**: Ensure NFC is enabled in device settings
- **Card not detected**: Try different card positions on phone back
- **Programming failed**: Check admin portal has valid data configured

### Admin Portal Issues
- **500 Error**: Check Railway logs for detailed error messages
- **Data not saving**: Verify JSON format is valid
- **App can't connect**: Confirm Railway URL is accessible

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📞 Support

For issues and questions:
- Create an issue in this repository
- Check existing issues for solutions
- Review the troubleshooting section above
