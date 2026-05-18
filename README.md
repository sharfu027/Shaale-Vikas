# Shaale-Vikas

Shaale-Vikas is an Android application designed to support rural schools by connecting donors with school development needs such as books, sanitation facilities, learning materials, infrastructure improvements, and educational resources.

The app allows users to explore active school needs, pledge support, and help improve educational opportunities for children.

---

## Features

* User Registration & Login
* View Active School Needs
* Pledge Support System
* Firebase Realtime Database Integration
* Firebase Authentication
* Dynamic School Need Cards
* Recent Pledges Display
* Modern Material UI Design
* Android Emulator & Real Device Support

---

## Tech Stack

### Frontend

* Kotlin
* XML Layouts
* Android SDK
* Material Design Components

### Backend & Services

* Firebase Authentication
* Firebase Realtime Database
* Firebase Storage

### Tools

* Android Studio
* Gradle
* Git & GitHub

---

## Project Structure

```bash
app/
 ├── manifests/
 ├── java/com/example/shaale_vikas/
 ├── res/
 │    ├── layout/
 │    ├── drawable/
 │    ├── values/
 └── build.gradle.kts
```

---

## Installation & Setup

### 1. Clone Repository

```bash
git clone https://github.com/sharfu027/Shaale-Vikas.git
```

### 2. Open in Android Studio

* Open Android Studio
* Click "Open"
* Select the cloned project folder

### 3. Firebase Setup

Add your Firebase configuration file:

```text
app/google-services.json
```

Enable:

* Firebase Authentication
* Firebase Realtime Database
* Firebase Storage

### 4. Sync Gradle

Allow Android Studio to complete Gradle sync.

### 5. Run Project

* Start Emulator or connect Android device
* Click Run ▶

---

## Firebase Rules (Development Mode)

### Firestore / Realtime Database Rules

```javascript
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

### Storage Rules

```javascript
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

> These rules are for development/testing only.

---

## Current Modules

* Authentication System
* Dashboard
* Need Listing
* Donation/Pledge UI
* Recent Pledges Adapter
* Firebase Data Integration

---

## Future Improvements

* Payment Gateway Integration
* Admin Dashboard
* NGO Verification
* Push Notifications
* Dark Mode
* Analytics Dashboard
* Multi-language Support

---

## Screenshots

*Add project screenshots here.*

---

## Author

**Mohammed Sharfuddin**

* GitHub: https://github.com/sharfu027

---

## License

This project is developed for educational and social impact purposes.
