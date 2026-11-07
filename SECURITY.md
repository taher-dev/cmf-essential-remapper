# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

---

## Offline-First Trust Model

CMF Essential Remapper was architected from line one with an uncompromising focus on user privacy and security:

1. **Zero Network Capabilities:**
   - The application does not declare `android.permission.INTERNET`. It is technically incapable of sending data off your device or connecting to any external server.
2. **Restricted Accessibility Scope:**
   - The Accessibility Service operates with `canRetrieveWindowContent="false"`. It cannot inspect UI elements, read screen contents, capture keystrokes, or intercept passwords.
   - It only listens for hardware key events matching the physical Essential Key.
3. **Local Storage Only:**
   - User preferences and assigned mappings are stored exclusively on-device via Android Jetpack DataStore.

---

## Reporting a Security Vulnerability

If you discover a potential security or privacy vulnerability, please do **NOT** open a public issue.

Instead, please send an email directly to the developer:
- **Email:** `monmoyzx@gmail.com`
- **Subject:** `[SECURITY] CMF Essential Remapper Vulnerability Report`

Please include:
- A description of the vulnerability.
- Steps to reproduce or proof-of-concept.
- Any potential impact.

You will receive an acknowledgment within 48 hours, and any confirmed vulnerabilities will be addressed in an expedited release.
