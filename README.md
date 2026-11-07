# CMF Essential Remapper

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-13%2B%20(API%2033%2B)-green.svg)](https://developer.android.com)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20%7C%20No%20Tracking-red.svg)](PRIVACY_POLICY.md)
[![Nothing OS](https://img.shields.io/badge/Design-Nothing%20OS%20Aesthetic-black.svg)](preview.html)

A lightweight, open-source Android utility designed specifically for **CMF by Nothing** devices (CMF Phone 1, CMF Phone 2 Pro) to remap the physical **Essential Key** hardware button to custom actions, ring mode cycling, and quick system shortcuts with a native Nothing OS HUD overlay.

---

## Features

- **Hardware Key Interception**: Intercepts CMF Essential Key presses (hardware scan code `250` or dynamically learned key code) without screen monitoring.
- **Multiple Gesture Recognition**: Supports customizable mappings for:
  - **Single Press**
  - **Double Press**
  - **Long Press**
- **Dual Ringer Cycling Modes**:
  - **Cycle Ring Mode 1**: `Ring → Vibrate → Silent → Ring`
  - **Cycle Ring Mode 2**: `Ring → Silent → Vibrate → Ring`
- **Toggleable System Actions**:
  - **Toggle Notifications**: Pulls down or dismisses the notification drawer.
  - **Toggle Quick Settings**: Expands or dismisses the quick settings panel.
  - **Toggle Flashlight**: Hardware camera torch on/off with state awareness.
  - **Take Screenshot**: Global screenshot capture.
  - **Lock Screen**: Immediately locks display without waking.
- **Nothing OS Visual State HUD Overlay**:
  - Sleek top-center matte capsule HUD overlay displaying state (`RING`, `VIBRATE`, `SILENT`, `TORCH ON/OFF`) with Nothing red status dot.
  - Non-intrusive, non-focusable, and non-touchable (`FLAG_NOT_TOUCHABLE`) — never interrupts active apps or games.
  - Can be toggled on or off in the Display preferences.
- **Audio Transition Engine**:
  - Smooth transitions across all ringer modes, bypassing AOSP `ZenModeHelper` vibration locks when cycling `VIBRATE → SILENT`.
- **100% Privacy & Zero Network**:
  - Zero internet permissions (`android.permission.INTERNET` is not requested).
  - `canRetrieveWindowContent="false"` ensures zero access to screen text or passwords.
  - Local settings stored on-device via Jetpack DataStore.

---

## Architecture & Tech Stack

- **UI Framework:** Jetpack Compose + Material 3 (Nothing OS Minimalist Dark Aesthetic)
- **Architecture Pattern:** Clean Architecture + MVI/MVVM with Kotlin Coroutines & Flow
- **Key Interception:** Android `AccessibilityService` (`FLAG_REQUEST_FILTER_KEY_EVENTS`)
- **Overlay Window:** `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY`
- **Local Persistence:** Jetpack Preferences DataStore
- **Optional Privilege Bridge:** Shizuku API (for freezing conflicting OEM system apps or direct privileged audio binder calls)

```
com.monmoy.cmfessentialremapper/
│
├── data/
│   ├── preferences/DataStoreManager.kt         # Jetpack DataStore for local persistence
│   └── repository/RingerModeRepositoryImpl.kt  # Audio framework & Shizuku binder IPC
│
├── domain/
│   ├── model/KeyAction.kt                      # Customizable key actions & gestures
│   ├── model/RingerMode.kt                     # SILENT, VIBRATE, RING modes
│   └── usecase/CycleRingerModeUseCase.kt       # Dual ringer cycle order logic
│
├── presentation/
│   ├── MainActivity.kt                         # Single Activity edge-to-edge container
│   ├── home/HomeScreen.kt                      # Main dashboard & configuration
│   ├── components/GestureActionCard.kt         # Nothing OS gesture assignment cards
│   ├── components/PrivacyPolicyDialog.kt       # In-app privacy disclosure
│   ├── overlay/StateOverlayManager.kt          # Floating Nothing OS capsule HUD
│   └── setup/SetupScreen.kt                    # Step-by-step permissions wizard
│
├── service/
│   └── EssentialKeyAccessibilityService.kt     # Hardware key filtering & gesture engine
│
└── utils/
    ├── KeyGestureClassifier.kt                 # Finite state machine for key gestures
    └── EssentialSpaceManager.kt                # Package management for OEM conflicting apps
```

---

## Setup & Installation

1. **Build and Install:**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Grant Required Permissions:**
   - **Accessibility Service:** Settings → Accessibility → Enable **CMF Essential Remapper**.
   - **Do Not Disturb Access (Notification Policy Access):** Required on modern Android for entering Silent mode.
   - **Battery Optimization:** Whitelist the app so the accessibility service remains persistent in the background.

3. *(Optional)* **Disable OEM Essential Space:**
   On stock Nothing OS, pressing the Essential Key may launch Nothing's default Essential Space or Recorder. If you have Shizuku active, CMF Essential Remapper can freeze those two packages directly from the Setup screen with one tap.

---

## Privacy Policy

CMF Essential Remapper is built on strict open-source privacy values:
- **No Internet Access:** The app has no network access and cannot transmit data.
- **No Analytics / Telemetry:** No tracking, no device IDs, no advertising SDKs.
- **No Screen Reading:** The service cannot read screen text, passwords, or messages.

Read the full [Privacy Policy](PRIVACY_POLICY.md).

---

## Interactive UI Preview

An interactive, responsive preview simulating the device UI, button mappings, and the HUD pill overlay is available in [`preview.html`](preview.html). Open it in any modern browser to test the interactive mockup.

---

## License

```
Copyright 2026 CMF Essential Remapper Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
