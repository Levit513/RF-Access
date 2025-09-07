# MIFARE Card Programming System

A comprehensive web-based system for programming MIFARE Classic cards using NFC-enabled Android devices.

## Features

- **Admin Interface**: Create and manage MIFARE card programs
- **Secure Distribution**: One-time access tokens with 24-hour expiry
- **NFC Programming**: Web-based NFC interface for Android Chrome
- **User Management**: Role-based access control
- **Sector Editor**: Visual interface for MIFARE card data

## Quick Start

1. Install dependencies: `pip install -r requirements.txt`
2. Run the application: `python run.py`
3. Open browser: http://localhost:5000
4. Login with: admin / admin123

## Deployment

This application is ready for cloud deployment with optimized dependencies for serverless environments. Hardware-dependent packages have been removed and replaced with web-based alternatives.

**Latest Update**: Removed all smartcard library dependencies for full cloud compatibility.

## Usage

1. **Admin creates programs** using the sector editor
2. **Admin distributes secure links** to registered users
3. **Users access links on Android devices**
4. **NFC programming** writes data to MIFARE cards

## Security

- One-time access tokens
- 24-hour link expiry
- Encrypted data transmission
- Role-based permissions

## Requirements

- Python 3.8+
- Android device with NFC
- Chrome browser on Android
- MIFARE Classic cards

## Project Structure

```
Mifare App/
├── main.py              # Main application entry point
├── mifare/              # Core MIFARE functionality
│   ├── __init__.py
│   ├── card_reader.py   # Card reader interface
│   ├── card_types.py    # MIFARE card type definitions
│   └── utils.py         # Utility functions
├── requirements.txt     # Python dependencies
└── README.md           # This file
```

## License

[Add license information]
