# Save Mobile Application - Android Frontend

This repository contains the Android application code for **Save**, a mobile platform designed for managing financial contributions, loans, and payouts within a group.

## 📱 Features

The application supports two distinct user roles with tailored interfaces and capabilities:

### Admin Features
- **Member Management**: Register and manage group members.
- **Transaction Oversight**: Approve or reject contributions and payouts.
- **Loan Control**: Review loan requests and manage repayment schedules.
- **Analytics Dashboard**: Comprehensive financial reports and group performance metrics.
- **System Settings**: Configure group rules, interest rates, and penalties.

### Member Features
- **Financial Tracking**: View contribution history and current balance.
- **Loan Services**: Apply for loans and track repayment status.
- **Upcoming Payments**: Reminders and schedules for contributions and loan installments.
- **Profile Management**: Maintain personal information and security settings.

## 🛠️ Tech Stack

- **Platform**: Android (Native)
- **Language**: Java / Kotlin (supports Android 7.0+ / API 24+)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: Room persistence library
- **Networking**: Retrofit 2 & OkHttp
- **Dependency Injection**: Gradle-based
- **UI Components**: 
    - Google Material Design
    - Lottie Animations (smooth micro-interactions)
    - MPAndroidChart (financial visualizations)
- **Security**: 
    - AndroidX Security (EncryptedSharedPreferences)
    - JWT-based authentication
- **Notifications**: Firebase Cloud Messaging (FCM)

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11
- Android SDK 36 (Compile SDK)
- A running instance of the [Save App Backend](../Backend/README.md)

### Installation
1. Clone the repository and navigate to the `Front-end` directory.
2. Open the project in Android Studio.
3. Sync project with Gradle files.
4. (Optional) Configure the base URL in `ApiService.java` or `network_security_config.xml` to point to your backend server.
5. Build and run the app on an emulator or physical device.

## 🏗️ Project Structure

```text
app/src/main/java/com/example/save/
├── data/           # Repositories, Models, and Local/Remote Data Sources
├── services/       # Background services (FCM, etc.)
├── ui/             # UI Components
│   ├── activities/ # Activities (Login, Main, Settings, etc.)
│   ├── fragments/  # Reusable UI fragments (Dashboard, List, etc.)
│   ├── adapters/   # RecyclerView adapters
│   └── viewmodels/ # MVVM ViewModels for data handling
└── utils/          # Helper classes and formatting utilities
```

## 🔒 Security

- Sensitive data such as authentication tokens are stored using `EncryptedSharedPreferences`.
- Network communication is secured via HTTPS (ensure backend is configured correctly).
- OTP-based registration and password recovery.

---
*Developed for the Save financial group management platform.*
