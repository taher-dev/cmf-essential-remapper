<div align="center">

<svg width="84" height="84" viewBox="0 0 108 108" fill="none" xmlns="http://www.w3.org/2000/svg">
  <rect width="108" height="108" rx="28" fill="#121212" stroke="#282828" stroke-width="2"/>
  <circle cx="54" cy="54" r="28" stroke="#FFFFFF" stroke-width="3"/>
  <rect x="48" y="40" width="12" height="28" rx="6" fill="#D71921"/>
</svg>

# CMF Essential Remapper

**Give your CMF Essential Key the powers it was meant to have.**

[![Release](https://img.shields.io/badge/Release-v1.0.0-D71921?style=for-the-badge&logo=android&logoColor=white)](https://github.com/taher-dev/cmf-essential-remapper/releases/latest)
[![License](https://img.shields.io/badge/License-Apache_2.0-181818?style=for-the-badge&logo=open-source-initiative&logoColor=white)](LICENSE)
[![Target](https://img.shields.io/badge/Device-CMF%20Phone%201%20%7C%202%20Pro-181818?style=for-the-badge&logo=nothing&logoColor=white)](https://nothing.tech)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-4CAF50?style=for-the-badge&logo=shield&logoColor=white)](PRIVACY_POLICY.md)

<br/>

[**Download Latest APK**](https://github.com/taher-dev/cmf-essential-remapper/releases/latest) • [**Interactive Web Preview**](https://htmlpreview.github.io/?https://github.com/taher-dev/cmf-essential-remapper/blob/main/preview.html) • [**Privacy Policy**](PRIVACY_POLICY.md) • [**Report an Issue**](https://github.com/taher-dev/cmf-essential-remapper/issues)

</div>

---

## ⚡ What is CMF Essential Remapper?

On stock **Nothing OS**, the physical **Essential Key** on CMF devices is often locked to OEM shortcuts like Essential Space or voice recorder.

**CMF Essential Remapper** is a lightweight, zero-bloat, open-source Android utility that intercepts the physical hardware key and unlocks customizable single-press, double-press, and long-press actions — complete with a sleek, native **Nothing OS floating capsule HUD** overlay.

> **100% Offline Promise:** No internet permissions (`android.permission.INTERNET` is omitted entirely). No analytics, no telemetry, no tracking. Your device stays entirely yours.

---

## ✨ Features

- 🔴 **Physical Hardware Key Interception:** Direct hardware filtering (scan code `250` or dynamic learning) without inspecting screen touch or reading content.
- 🎯 **3 Distinct Hardware Gestures:**
  - **Single Press**
  - **Double Press**
  - **Long Press**
- 🔔 **Dual Ring Mode Cycling:**
  - **Cycle Ring Mode 1:** `Ring → Vibrate → Silent → Ring`
  - **Cycle Ring Mode 2:** `Ring → Silent → Vibrate → Ring`
  *(Bypasses AOSP ZenModeHelper vibration lock when cycling between modes).*
- 🛠 **System Utility Actions:**
  - **Toggle Flashlight:** State-aware camera torch toggle.
  - **Toggle Notifications:** Opens notification shade or dismisses it on repeated press.
  - **Toggle Quick Settings:** Expands quick tiles or closes them on repeated press.
  - **Take Screenshot:** Instant global screen capture.
  - **Lock Screen:** One-click screen sleep.
- 💊 **Nothing OS Floating State Capsule HUD:**
  - Minimalist matte capsule pill HUD at the top center showing status (`RING`, `VIBRATE`, `SILENT`, `TORCH ON/OFF`) with signature red dot accent.
  - Uses `FLAG_NOT_TOUCHABLE` and `FLAG_NOT_FOCUSABLE` — never blocks touches, typing, or gaming.
  - Can be toggled on/off in the app settings.
- 🧊 **Optional Conflicting App Freeze via Shizuku:**
  - If OEM apps (Essential Space, Recorder) conflict with your hardware button, freeze them safely with one tap using Shizuku.

---

## 🚀 Quick Start & Installation

### Option 1: Direct APK Download (Recommended)
1. Go to [**Releases**](https://github.com/taher-dev/cmf-essential-remapper/releases/latest) and download the latest `CMF-Essential-Remapper-v1.0.0.apk`.
2. Install the APK on your **CMF Phone 1** or **CMF Phone 2 Pro**.

### Option 2: Install via ADB
```bash
adb install -r CMF-Essential-Remapper-v1.0.0.apk
```

---

## ⚙️ Permission Setup (Why they are needed)

CMF Essential Remapper strictly asks only for the permissions required to operate hardware shortcuts:

| Permission | Purpose | Why It's Safe |
| :--- | :--- | :--- |
| **Accessibility Service** | Detects physical hardware key press events | Configured with `canRetrieveWindowContent="false"`. Cannot read passwords, screen text, or personal messages. |
| **Notification Policy (DND)** | Enables setting device to Silent mode | Standard Android requirement to change ringer to Silent without muting alarms. |
| **Battery Optimization** | Keeps service active in the background | Prevents Nothing OS aggressive background killing. |
| **Shizuku** *(Optional)* | Disables conflicting OEM packages with one tap | Entirely optional; you can also disable OEM apps manually in Android Settings. |

---

## 📱 Interactive Web Preview

Experience the app interface, try button mappings, and test the floating HUD pill right in your browser without installing anything!

👉 **[Launch Interactive Web Preview](https://htmlpreview.github.io/?https://github.com/taher-dev/cmf-essential-remapper/blob/main/preview.html)**

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/taher-dev/cmf-essential-remapper.git
cd cmf-essential-remapper

# Run tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Install to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Privacy & Transparency

CMF Essential Remapper is built on transparency:
- **Zero Internet Access:** The app does not request `android.permission.INTERNET`. It cannot send data over Wi-Fi, cellular, or Bluetooth.
- **Zero Analytics / SDKs:** No Firebase, no Google Analytics, no crash reporters.
- **Local Storage Only:** Preferences are saved on your phone via Jetpack DataStore.

Read our full [**Privacy Policy**](PRIVACY_POLICY.md).

---

## 👤 Developer & Maintainer

Crafted by **Taher Mahmud Monmoy**:
- 🌐 **Portfolio:** [monmoy.pages.dev](https://monmoy.pages.dev/)
- 💻 **GitHub:** [@taher-dev](https://github.com/taher-dev)
- 💼 **LinkedIn:** [linkedin.com/in/taher-mahmud-monmoy](https://linkedin.com/in/taher-mahmud-monmoy)

> *"Just a developer based in Bangladesh who likes building cool stuff, tweaking hardware, and making lightweight tools that make tech feel a little better."*

---

## 📄 License

Licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.
