# Privacy Policy for CMF Essential Remapper

**Last Updated:** November 2026  
**License:** Apache License, Version 2.0  
**Project:** CMF Essential Remapper (Open Source)

CMF Essential Remapper is an open-source, privacy-first utility designed to remap the physical Essential Key on CMF by Nothing devices (such as the CMF Phone 1 and CMF Phone 2 Pro).

We firmly believe that personal utilities should never track their users. This document outlines our strict, non-negotiable privacy principles.

---

## 1. 100% Offline — Zero Network Access

- **No Internet Permission:** The application does not request, declare, or possess the `android.permission.INTERNET` permission in its Android Manifest.
- **Zero Remote Communication:** The app is architecturally incapable of connecting to the internet, opening network sockets, making API calls, or transmitting data to any remote server or third party.

---

## 2. Zero Telemetry & Analytics

- **No Tracking:** We do not collect, track, or record any personal information, device serial numbers, IMEI, advertising IDs, or behavioral analytics.
- **No Crash Reporting SDKs:** There are no embedded crash reporting frameworks (such as Firebase Crashlytics or Sentry). Any diagnostics or event logs remain strictly in volatile on-device memory and are cleared when the app or service terminates.
- **No Advertising:** The application contains zero advertisements, trackers, or commercial monetization SDKs.

---

## 3. Scoped Accessibility Service Disclosure

CMF Essential Remapper uses an Android Accessibility Service (`EssentialKeyAccessibilityService`) solely to detect physical button presses of the Essential Key hardware button.

- **`canRetrieveWindowContent="false"`:** The service is explicitly configured with `canRetrieveWindowContent="false"`. It cannot inspect, access, read, or log:
  - Screen content, layouts, or user interfaces
  - Text fields, keystrokes, or passwords
  - Personal messages, photos, emails, or contacts
  - Content of notifications from other apps
- **`flagRequestFilterKeyEvents`:** The service is granted only key event filtering flags. When a hardware key event is received, it checks whether the key code or scan code matches the physical Essential Key (scan code `250`). All other hardware events (such as power, volume up/down, or navigation) are passed through untouched.
- **`TYPE_ACCESSIBILITY_OVERLAY`:** The visual HUD overlay is rendered locally on top of the screen using the Accessibility Service overlay window type. It is non-focusable and non-touchable (`FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE`), and does not intercept touch events from other apps.

---

## 4. Local On-Device Storage

All user settings, including:
- Gesture action mappings (Single Press, Double Press, Long Press)
- Learned custom hardware scan codes
- Visual overlay display preferences
- Haptic feedback toggles

are stored **strictly locally** on your device using Android Jetpack DataStore preferences. These preferences are private to the application sandbox and are never synced to any cloud services.

---

## 5. Shizuku Integration (Optional)

For users who optionally choose to run Shizuku:
- Shizuku is used strictly locally via Binder IPC on the device to execute privileged audio transitions (`setRingerModeInternal`) or freeze default OEM packages (`com.nothing.ntessentialspace`, `com.nothing.ntessentialrecorder`).
- No commands are sent outside the local device shell.

---

## 6. Open Source & Auditability

CMF Essential Remapper is free and open-source software. You are encouraged to inspect, review, audit, or build the entire codebase yourself:

- **Repository:** Public Open Source
- **License:** [Apache License 2.0](LICENSE)

If you have questions, concerns, or contributions regarding security or privacy, please open an issue or pull request in the repository.
