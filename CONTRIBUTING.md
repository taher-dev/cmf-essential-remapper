# Contributing to CMF Essential Remapper

First off, thank you for considering contributing to **CMF Essential Remapper**! 🎉

This project is an independent open-source utility designed to give CMF & Nothing device owners total control over their hardware Essential Key without advertisements, bloat, or tracking.

---

## Code of Conduct

We are committed to providing a friendly, safe, and welcoming environment for all contributors. Please be respectful and constructive in all discussions, issues, and pull requests.

---

## Development Setup

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer recommended)
- **JDK 17** (Temurin or OpenJDK)
- **Android SDK Platform 34** (API 34)
- **Physical Device:** CMF Phone 1 or CMF Phone 2 Pro with USB Debugging enabled (recommended for physical hardware key testing).

### Cloning & Building
```bash
git clone https://github.com/taher-dev/cmf-essential-remapper.git
cd cmf-essential-remapper

# Run Unit Tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Install Directly to Connected Device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Architectural Principles

1. **Strict Offline Model:**
   - Under no circumstances should `android.permission.INTERNET` or network libraries be introduced.
2. **Clean Architecture:**
   - `data`: Hardware access, DataStore preferences, Shizuku bindings.
   - `domain`: Pure business logic, models (`KeyAction`, `RingerMode`), and UseCases.
   - `presentation`: Jetpack Compose UI adhering to Nothing OS minimalist dark aesthetics.
   - `service`: Android AccessibilityService for low-level hardware key interception.
3. **Synchronized Preview:**
   - If UI changes or new button actions are introduced, update [`preview.html`](preview.html) so web users can preview the interface before installation.

---

## Submitting Pull Requests

1. Fork the repository and create your branch from `develop`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Make your changes with clear, descriptive commit messages.
3. Ensure all tests pass:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. Push your branch to GitHub and open a Pull Request targeting the `develop` branch.
5. Fill out the provided Pull Request template.

Thank you for helping make CMF Essential Remapper better for everyone!

