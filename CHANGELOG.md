# Changelog

All notable changes to **CMF Essential Remapper** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-07

### 🎉 Initial Public Release

Welcome to the first open-source release of **CMF Essential Remapper**, purpose-built for Nothing and CMF devices (CMF Phone 1, CMF Phone 2 Pro).

#### Key Features & Highlights
- **Hardware Button Interception:** Intercept physical Essential Key hardware events (scan code `250` or dynamic learning) without intercepting general touch or screen content.
- **Gesture Support:** Configure independent actions for **Single Press**, **Double Press**, and **Long Press**.
- **Dual Ringer Cycle Modes:**
  - *Mode 1:* `Ring → Vibrate → Silent → Ring`
  - *Mode 2:* `Ring → Silent → Vibrate → Ring`
- **Toggleable Actions:**
  - *Toggle Notifications:* Pull down or close the notification shade.
  - *Toggle Quick Settings:* Expand or dismiss Quick Settings with repeated presses.
  - *Toggle Flashlight:* Hardware torch toggle with state awareness.
  - *Take Screenshot & Lock Screen:* Instant system shortcuts.
- **Nothing OS Floating State HUD:**
  - Non-intrusive top-center matte capsule pill displaying active state with a signature Nothing red dot.
  - Can be toggled on/off in Display settings.
- **100% Privacy Guarantee:**
  - Zero internet permission (`android.permission.INTERNET` not present in manifest).
  - Strictly offline storage using Jetpack DataStore.
- **Nothing OS Dark Aesthetic:**
  - Deep matte dark interface (`#121212`), 1dp borders, red accent details, and Space Grotesk typography.
- **Developer & About Dialog:**
  - Built-in developer attribution dialog with direct links to GitHub ([`taher-dev`](https://github.com/taher-dev)) and Portfolio ([`monmoy.pages.dev`](https://monmoy.pages.dev)).
