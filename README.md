Save (Front-end) - Savings Circle Management
A professional-grade Android application designed to digitize informal savings groups (Chamas/Saccos). This app focuses on financial transparency, automated contribution tracking, and secure group fund management.
🚀 The Problem
Informal savings groups often suffer from a lack of transparency, manual bookkeeping errors, and security risks associated with cash handling. Save provides a digital ledger where every member can track group progress, view their own equity, and access emergency loans with 100% visibility.
🛠 Tech Stack
•
Language: Java (Modern Android standards)
•
Architecture: MVVM (Model-View-ViewModel)
•
Networking: Retrofit 2 + OkHttp (with custom JWT interceptors)
•
Local Storage: Room Persistence Library & EncryptedSharedPreferences
•
UI/UX: Material Components (M3), ConstraintLayout for complex hierarchies
•
Data Vis: MPAndroidChart (Real-time financial reporting)
•
Security: AES-256 GCM encryption via Android KeyStore
✨ Key Features
•
Financial Dashboard: Real-time visualization of group savings, interest earned, and loan distribution using line and pie charts.
•
Automated Tracking: Individual contribution history with status indicators (Active/Shortfall).
•
Security First:
◦
Sensitive session data stored in EncryptedSharedPreferences.
◦
JWT-based authentication with automatic token injection in API headers.
◦
Business logic offloaded to a secure Node.js/Python backend.
•
Exportable Reports: One-tap export for PDF, Excel, and CSV financial statements.
•
Dynamic Configuration: Admins can set interest rates, penalties, and payout cycles directly through the UI.
📦 Getting Started
Prerequisites
•
Android Studio Ladybug (or newer)
•
Minimum SDK: 26 (Android 8.0)
•
Target SDK: 34 (Android 14)
Installation
1.
Clone the repository:
Shell Script
git clone https://github.com/yourusername/save-frontend.git
2.
Open the project in Android Studio.
3.
Create a local.properties file in the root directory and specify your SDK path:
Properties
properties
sdk.dir=/path/to/your/android/sdk
4.
Sync Gradle and run the :app module.
🛡 Security Implementation
Unlike many basic projects, this app implements Bank-Grade Security patterns:
•
MasterKey API: Used to manage encryption keys in the hardware-backed Android KeyStore.
•
Safe Interceptors: A custom OkHttpClient interceptor checks for token expiration and handles 401 unauthorized errors globally.
•
Data Integrity: All financial calculations are handled server-side to prevent client-side manipulation of savings values.
📈 Roadmap
•
[ ] Integration with Mobile Money APIs (MTN/Airtel UG).
•
[ ] Push notifications for contribution reminders via Firebase Cloud Messaging.
•
[ ] Multi-language support (Luganda/Swahili).
📄 License
Internal / Private. See the SECURITY_NOTES.md for details on the backend integration.
