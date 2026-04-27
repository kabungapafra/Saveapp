# 💰 Save — Savings Circle Management

> A professional-grade Android application that digitizes informal savings groups (Chamas / ROSCAs), bringing financial transparency, automated contribution tracking, and secure fund management to every member.

---

## 🚀 The Problem

Informal savings groups suffer from a lack of transparency, manual bookkeeping errors, and the security risks of cash handling. **Save** replaces the paper ledger with a real-time digital system — every member can track group progress, view their own equity, and access emergency loans with full visibility.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (Modern Android standards) |
| Architecture | MVVM (Model-View-ViewModel) |
| Networking | Retrofit 2 + OkHttp (custom JWT interceptors) |
| Local Storage | Room Persistence Library & EncryptedSharedPreferences |
| UI/UX | Material Design 3, ConstraintLayout |
| Data Visualisation | MPAndroidChart (real-time financial charts) |
| Security | AES-256 GCM via Android KeyStore |

---

## ✨ Key Features

### 📊 Financial Dashboard
Real-time visualisation of group savings, interest earned, and loan distribution using line and pie charts powered by MPAndroidChart.

### 🔄 Automated Contribution Tracking
Individual contribution history with clear status indicators — **Active** or **Shortfall** — so no member is ever in the dark.

### 🔒 Security First
- Sensitive session data stored in `EncryptedSharedPreferences`
- JWT-based authentication with automatic token injection via OkHttp interceptors
- All financial calculations handled server-side to prevent client-side manipulation

### 📄 Exportable Reports
One-tap export of full financial statements in **PDF**, **Excel**, and **CSV** formats.

### ⚙️ Dynamic Admin Configuration
Admins can configure interest rates, penalty rules, and payout cycles directly from within the app.

---

## 📦 Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- Minimum SDK: **26** (Android 8.0)
- Target SDK: **34** (Android 14)

### Installation

1. **Clone the repository**
```bash
   git clone https://github.com/yourusername/save-frontend.git
```

2. **Open the project** in Android Studio.

3. **Configure your SDK path** — create a `local.properties` file in the root directory:
```properties
   sdk.dir=/path/to/your/android/sdk
```

4. **Sync Gradle** and run the `:app` module.

---

## 🛡 Security Implementation

This app follows bank-grade security patterns:

- **MasterKey API** — encryption keys managed in the hardware-backed Android KeyStore
- **Safe Interceptors** — a custom `OkHttpClient` interceptor handles token expiration and 401 Unauthorized errors globally
- **Data Integrity** — all financial calculations are offloaded to the backend, preventing client-side tampering

---

## 📈 Roadmap

- [ ] Mobile Money integration (MTN Uganda / Airtel Uganda)
- [ ] Push notifications for contribution reminders (Firebase Cloud Messaging)
- [ ] Multi-language support (Luganda / Swahili)

---

## 📄 License

Internal / Private. See `SECURITY_NOTES.md` for backend integration details.
