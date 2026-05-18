# Shaale-Vikas

Shaale-Vikas is an Android application designed to support rural schools by connecting donors with school development needs such as books, sanitation facilities, learning materials, infrastructure improvements, and educational resources.

The app allows users to explore active school needs, pledge support, and help improve educational opportunities for children.

---

## Features

* **User Registration & Login**: Secure authentication for Alumni and Admins.
* **Role-Based Access Control**: Different interfaces for Admins (Management) and Alumni (Donors).
* **Admin Dashboard**: Specialized panel for school administrators to approve/reject pledges and track impact.
* **Analytics Dashboard**: Real-time tracking of total funding and completed project counts.
* **School Needs Management**: Admins can Add and Delete school needs.
* **Pledge Support System**: Users can commit to helping specific needs.
* **Category Filtering**: Easily browse needs by category (Infrastructure, Learning Materials, etc.).
* **Donation Approval**: Verification system where Admins approve or reject pledges.
* **Automatic Progress Tracking**: Project funding progress updates automatically upon pledge approval.
* **Supporters List**: View community members who have pledged for a specific project.
* **Before/After Progress**: Visual tracking of project completion with photo uploads.
* **Hall of Fame**: Recognition for donors ranked by their total contribution.
* **Profile & Personal History**: Users can view their total impact and full pledge history with status tracking (Pending/Approved/Rejected).
* **Multi-language Support**: Full support for **Kannada** and English languages.
* **Modern Material UI**: Clean, intuitive interface using Material 3 components.

---

## Tech Stack

### Frontend
* Kotlin
* XML Layouts
* View Binding
* Jetpack Components (ViewModel, Lifecycle)
* Glide (Image Loading)

### Backend & Services
* Firebase Authentication
* Firebase Cloud Firestore (NoSQL Database)
* Firebase Storage (Image Hosting)

---

## Project Structure

```bash
app/
 ├── manifests/
 ├── java/com/example/shaale_vikas/
 │    ├── AdminDashboardActivity.kt  # Admin Panel & Approval Logic
 │    ├── MainActivity.kt            # Donor Dashboard & Need Listing
 │    ├── ProfileActivity.kt         # User Profile & Pledge History
 │    ├── DonorsActivity.kt          # Hall of Fame (Top Contributors)
 │    ├── NeedDetailActivity.kt      # Detailed view with supporters list
 ├── res/
 │    ├── layout/
 │    ├── drawable/                  # Includes status badges & custom shapes
 │    ├── values/                    # English strings and themes
 │    ├── values-kn/                 # Kannada localization
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
* Cloud Firestore
* Firebase Storage

### 4. Admin Setup
To access the Admin features:
1. Register a user in the app.
2. Go to Firebase Console -> Firestore.
3. Find your user document in the `users` collection.
4. Change the `role` field from `"ALUMNI"` to `"ADMIN"`.

---

## Firebase Rules (Recommended)

### Firestore Rules
```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## Future Improvements
* Payment Gateway Integration (Razorpay/UPI)
* Push Notifications for new needs
* Dark Mode Support

---

## Author
**Mohammed Sharfuddin**
* GitHub: https://github.com/sharfu027

---

## License
This project is developed for educational and social impact purposes.
