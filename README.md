# CMF Essential Remapper

A native Android application designed specifically for the **CMF Phone 2 Pro** to remap the dedicated **Essential Key** into a hardware-style three-stage ring mode controller (Silent, Vibrate, Ring).

---

## 1. Project Overview & Progress

- **Target Device:** CMF Phone 2 Pro (Android 14 / Nothing OS 2.6+)
- **Package Name:** `com.monmoy.cmfessentialremapper`
- **Minimum SDK:** 33 (Android 13)
- **Target SDK:** 35 (Android 15)
- **UI Engine:** Jetpack Compose + Material 3 (Nothing OS Minimalist Dark Aesthetic)
- **Architecture:** Clean Architecture + MVVM

### Stages:
- [x] **Step 1: Project Foundation** — Clean architecture, Nothing OS design system, Gradle & build pipeline.
- [x] **Step 2: Ringer Mode Engine** — Reliable AudioManager ringer mode control, Notification Policy Access (DND) management, interactive Compose indicators, lifecycle awareness, and comprehensive test suite.
- [ ] **Step 3: Hardware Key Interception & Accessibility Bridge** *(Pending Approval)*.

---

## 2. Package Architecture

```
com.monmoy.cmfessentialremapper/
│
├── data/
│   ├── preferences/
│   │   └── DataStoreManager.kt               # Jetpack DataStore implementation for local settings
│   └── repository/
│       ├── RingerModeRepository.kt           # Interface: live observation, mode setting, DND status
│       └── RingerModeRepositoryImpl.kt       # AudioManager & NotificationManager integration
│
├── domain/
│   ├── model/
│   │   ├── RingerMode.kt                     # SILENT, VIBRATE, RING + cycling & policy requirements
│   │   └── EssentialKeyStatus.kt             # Hardware configuration status (NotConfigured, Ready)
│   └── usecase/
│       ├── GetRingerModeUseCase.kt           # Observes live system ringer changes
│       ├── SetRingerModeUseCase.kt           # Validates and sets mode with DND checks
│       ├── CheckNotificationPolicyAccessUseCase.kt # Queries DND policy permission
│       └── GetEssentialKeyStatusUseCase.kt   # Observes key mapping status
│
├── presentation/
│   ├── MainActivity.kt                       # Single Activity, edge-to-edge, onResume permission check
│   ├── home/
│   │   ├── HomeScreen.kt                     # Compose screen with interactive modes & DND notice
│   │   ├── HomeViewModel.kt                  # StateFlow coordination, concurrency lock, error handling
│   │   └── HomeUiState.kt                    # Immutable state (modes, permission dialog, loading)
│   ├── components/
│   │   ├── ModeIndicatorCard.kt              # Interactive Silent / Vibrate / Ring cards with Material 3 ripple
│   │   ├── PermissionDialog.kt               # Nothing-styled DND permission explanation & settings button
│   │   ├── KeyStatusCard.kt                  # Essential Key status indicator ("Not Configured")
│   │   ├── NothingHeader.kt                  # Header with status LED and settings launcher
│   │   └── SettingsDialog.kt                 # Architecture and step roadmap dialog
│   └── theme/
│       ├── Color.kt                          # Nothing Black (#080808) & CMF Red Accent (#D71921)
│       ├── Theme.kt                          # Material 3 dark-first theme
│       └── Type.kt                           # Clean Sans & JetBrains Mono typography
│
├── receiver/
│   └── RingerModeReceiver.kt                 # BroadcastReceiver for AudioManager.RINGER_MODE_CHANGED_ACTION
│
├── service/
│   └── ServiceNotice.kt                      # Architecture boundary: AccessibilityService reserved for Step 3
│
└── utils/
    └── AudioUtils.kt                         # Conversion utilities between Android framework & domain
```

---

## 3. Ringer Mode & Permission Management

### Ringer Modes Supported:
1. **SILENT (`AudioManager.RINGER_MODE_SILENT`)**:
   - Switching into Silent mode on modern Android requires **Notification Policy Access (Do Not Disturb access)**.
   - If permission is missing when tapped, a compact dialog explains why the permission is required and provides a direct shortcut to system settings (`Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS`).
   - When the user returns to the app, permission is rechecked automatically in `MainActivity.onResume()`.
2. **VIBRATE (`AudioManager.RINGER_MODE_VIBRATE`)**:
   - Switches the phone to vibrate mode immediately without requiring special DND permissions.
3. **RING (`AudioManager.RINGER_MODE_NORMAL`)**:
   - Restores normal ringer volume and alerts.

### Concurrency & Redundancy Protections:
- **No-Op Protection:** Tapping the currently active mode produces no redundant system operations.
- **Race Condition Guard:** `isChangingMode` lock prevents multiple simultaneous mode switches.
- **Verification Guarantee:** The UI indicator state is driven by live `AudioManager` observations through `callbackFlow` and `RingerModeReceiver`.

---

## 4. Manual Testing Procedure on CMF Phone 2 Pro / Emulator

1. **Install the Debug APK:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Test Direct Mode Switching (Vibrate & Ring):**
   - Tap **Vibrate**: Observe the phone vibrate, the red indicator switch to Vibrate, and status bar display vibrate icon.
   - Tap **Ring**: Observe the phone exit vibrate mode, and the red indicator switch to Ring.
3. **Test DND Permission Flow (Silent Mode):**
   - Tap **Silent** (without granting DND permission beforehand).
   - Verify that the **Notification Policy Access** dialog appears with clear explanation.
   - Tap **OPEN SETTINGS**: The app opens Android's Special App Access -> Do Not Disturb access page.
   - Toggle **Allow** for *CMF Essential Remapper*, then tap Back to return to the app.
   - Verify that the app automatically recognizes the permission on resume and the warning badge disappears.
   - Tap **Silent** again: Verify the phone enters Silent mode.
4. **Test External Changes (Physical Volume Keys):**
   - Change the ringer mode using the phone's physical volume rocker or quick settings tile.
   - Verify that the app's `[LIVE FEED]` indicator automatically updates to reflect the new system state in real time.

---

## 5. Build Commands

```bash
# Execute unit test suite
./gradlew.bat testDebugUnitTest

# Run Android Lint
./gradlew.bat lintDebug

# Build debug APK
./gradlew.bat assembleDebug
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk`
