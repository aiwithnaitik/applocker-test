# Privacy Policy — AppLock Privacy Guard

**Last Updated:** October 2026  
**Effective Date:** October 2026  
**Application:** AppLock Privacy Guard (`com.applock.privacy`)  

---

## 1. Introduction & Overview

AppLock Privacy Guard ("we", "our", or "the App") is developed with a strict **Privacy-First, Zero-Knowledge** architecture. Our core philosophy is simple: **your private data belongs exclusively to you and never leaves your device.**

We do not operate remote user tracking servers, do not maintain remote user databases, and do not transmit your protected apps, media vault contents, PINs, patterns, or browsing habits to any third-party or remote servers.

---

## 2. Information We Access & Process

All processing described below occurs **strictly on-device in real-time** and is never collected, stored, or sold to third parties:

### 2.1 Usage Stats (`PACKAGE_USAGE_STATS`)
* **Purpose:** To detect in real-time when an application you have explicitly selected for protection is launched or brought to the foreground.
* **Storage & Transmission:** Evaluated in volatile memory only. Historical usage patterns are **never** logged, collected, or uploaded.

### 2.2 System Overlay (`SYSTEM_ALERT_WINDOW`)
* **Purpose:** To render the secure PIN/Pattern/Biometric lock screen on top of protected applications before unauthorized access can occur.
* **Storage & Transmission:** Purely visual on-device window rendering.

### 2.3 Camera (`android.permission.CAMERA`)
* **Purpose:** Exclusively used for the optional **Intruder Selfie** feature when enabled by the user. If an incorrect PIN or Pattern is repeatedly entered, the front-facing camera silently snaps an evidentiary photo.
* **Storage & Transmission:** Captured photos and attempt timestamps are saved exclusively in your device's isolated app-private internal storage (`context.filesDir`). Photos are never uploaded to the cloud or shared with anyone.

### 2.4 Biometric Data (`USE_BIOMETRIC`)
* **Purpose:** To allow you to unlock your protected applications using your device's fingerprint or face sensor.
* **Storage & Transmission:** The App uses the official Android `BiometricPrompt` framework. Biometric authentication is handled strictly by your device's hardware Secure Element / Trusted Execution Environment (TEE). The App **never** has access to, stores, or processes raw biometric data.

### 2.5 Private Media Vault
* **Purpose:** Allows you to hide sensitive photos and videos from your system gallery.
* **Storage & Transmission:** Vaulted files are stored in an encrypted, app-private directory (`filesDir/private_vault/`) accessible only within the App. Vault media is never backed up remotely or synced to external servers.

### 2.6 Notification Privacy Shield (`BIND_NOTIFICATION_LISTENER_SERVICE`)
* **Purpose:** When enabled, conceals incoming message previews and sensitive notifications belonging to locked applications from the status bar.
* **Storage & Transmission:** Notifications are filtered locally on-device. No notification text is collected, stored, or transmitted.

### 2.7 Website Blocker (`BIND_ACCESSIBILITY_SERVICE`)
* **Purpose:** Optional focus tool that inspects browser URL address bar fields to block user-specified distracting or malicious domains.
* **Storage & Transmission:** Operates 100% on-device. URLs are matched against your local blocklist and are never recorded, logged, or sent over the internet.

### 2.8 Internet Access (`android.permission.INTERNET`)
* **Purpose:** Used strictly for:
  1. Checking for official in-app software updates via GitHub Releases API.
  2. Rendering pages inside the built-in isolated Private Browser.
* **Storage & Transmission:** No analytics, crash telemetry, or identifiers are transmitted.

---

## 3. Data Retention & Deletion

* **Authentication Credentials:** Your master PIN and Pattern are stored locally using SHA-256 / salted hash representations in encrypted Android DataStore preferences.
* **Data Deletion:** You can delete all AppLock data, media vault items, intruder logs, and preferences at any time by clearing storage in Android Settings -> App Info -> AppLock -> Storage & Cache -> Clear Storage, or by uninstalling the application.

---

## 4. Children's Privacy

AppLock Privacy Guard does not knowingly collect or solicit any personal information from children under the age of 13. The application is a security utility intended for general audiences.

---

## 5. Changes to This Privacy Policy

We may update this Privacy Policy periodically. Any revisions will be reflected with an updated "Last Updated" date and distributed via app updates.

---

## 6. Contact Us

If you have questions or concerns regarding this Privacy Policy or your privacy while using AppLock:
* **Developer:** AppLock Privacy Team
* **Repository:** [https://github.com/aiwithnaitik/applocker-test](https://github.com/aiwithnaitik/applocker-test)
