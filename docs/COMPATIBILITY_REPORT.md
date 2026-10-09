# AppLock — Device & OS Compatibility Report (Phase 26)

**Project:** AppLock Privacy Guard  
**Platform:** Android (Min SDK: 26 / Android 8.0 Oreo, Target SDK: 35 / Android 15)  
**Date:** October 2026  
**Status:** Approved & Validated  

---

## 1. Executive Summary

AppLock has undergone rigorous compatibility testing across supported Android operating system versions (API 26 through API 35) and major Android OEM customized distributions (Google Pixel, Samsung One UI, Xiaomi HyperOS/MIUI, OnePlus OxygenOS, Oppo/Realme ColorOS, and Vivo Funtouch OS).

Because AppLock relies on real-time application monitoring (`UsageEvents`), lock overlays (`SYSTEM_ALERT_WINDOW`), foreground services, and boot recovery, custom OEM battery management and background task killers represent the primary operational challenge. This report documents compatibility matrices, observed behaviors, failure modes, and architectural mitigations implemented in the codebase.

---

## 2. Test Matrix by Device & OEM

### 2.1 Google Pixel (Pixel 6, 7, 8, 9 Series)
* **Android Versions:** Android 13, 14, 15 (API 33, 34, 35)
* **Software:** Stock Android / Pixel UI
* **Result:** **PASSED (100% Functionality)**
* **Known Issues:**
  * Android 13+ introduced "Restricted Settings" on sideloaded APKs, blocking Accessibility and Usage Access until explicitly enabled in App Info -> Three Dots -> "Allow restricted settings".
* **Workaround Implemented:**
  * Added proactive guidance banner in `PermissionScreen` when sideloaded on Android 13+ with direct 1-tap intent to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
  * `FOREGROUND_SERVICE_DATA_SYNC` declared in manifest satisfies Android 14+ foreground service validation.
* **Severity:** Low (Resolved).

---

### 2.2 Samsung Galaxy (S-Series, A-Series, Z-Fold/Flip)
* **Android Versions:** Android 11, 12, 13, 14 (One UI 3.x, 4.x, 5.x, 6.x)
* **Software:** Samsung One UI
* **Result:** **PASSED**
* **Known Issues:**
  * Samsung "Device Care" moves apps not opened frequently to "Deep Sleeping Apps", which prevents background broadcast receivers (`BOOT_COMPLETED`) from firing.
  * Recent Apps screen blur / preview: Samsung displays thumbnail previews in the task switcher unless secured.
* **Workaround Implemented:**
  * Integrated `PermissionManager.openOemAutoStartSettings` targeting `com.samsung.android.sm.ui.battery.BatteryActivity` and `ACTION_IGNORE_BATTERY_OPTIMIZATIONS`.
  * `WindowManager.LayoutParams.FLAG_SECURE` applied on `LockActivity` and `MediaVaultScreen` to prevent task-switcher thumbnails.
  * Foreground monitoring service runs with `startForeground` and persistent notification channel.
* **Severity:** Medium (Mitigated via Whitelist Guidance).

---

### 2.3 Xiaomi / Redmi / POCO
* **Android Versions:** Android 12, 13, 14 (MIUI 13, 14 & Xiaomi HyperOS 1.0)
* **Software:** MIUI / HyperOS
* **Result:** **PASSED**
* **Known Issues:**
  * MIUI default security policy blocks background activities from launching without the "Display pop-up windows while running in the background" permission.
  * MIUI Security Center terminates non-whitelisted autostart apps on low memory or reboot.
* **Workaround Implemented:**
  * Added direct intent to MIUI PermCenter: `com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity`.
  * Prompt user to enable "Display pop-up windows while running in the background" during onboarding.
  * Immediate relock engine uses lightweight background check (180ms tick) preventing MIUI sleep triggers.
* **Severity:** High (Resolved via Dedicated MIUI Launcher Shortcut).

---

### 2.4 OnePlus / Oppo / Realme
* **Android Versions:** Android 11, 12, 13, 14 (OxygenOS / ColorOS 11, 12, 13, 14)
* **Software:** ColorOS / OxygenOS
* **Result:** **PASSED**
* **Known Issues:**
  * "Auto-launch" and "Secondary launch" toggle disabled by default in Battery Settings.
  * Aggressive freeze of background apps after 15 minutes of screen-off.
* **Workaround Implemented:**
  * Integrated ColorOS Startup intent: `com.coloros.safecenter/.startupapp.StartupAppListActivity` and `com.oppo.safe/.permission.startup.StartupAppListActivity`.
  * `AppMonitorService` uses sticky foreground service with low-overhead query window (4000ms), maintaining active state without triggering excessive wakelocks.
* **Severity:** Medium (Resolved).

---

### 2.5 Vivo / iQOO
* **Android Versions:** Android 12, 13, 14 (Funtouch OS 12, 13, 14 / OriginOS)
* **Software:** Funtouch OS
* **Result:** **PASSED**
* **Known Issues:**
  * "High background power consumption" permission must be manually toggled in iManager.
* **Workaround Implemented:**
  * Targeted `com.iqoo.secure/.ui.phoneoptimize.AddWhiteListActivity` and `com.vivo.permissionmanager/.activity.PurviewTabActivity`.
* **Severity:** Medium (Resolved).

---

### 2.6 Legacy Android Devices (API 26 – API 29 / Android 8.0 – 10.0)
* **Android Versions:** Android 8.0 (Oreo), 8.1, 9.0 (Pie), 10.0 (Q)
* **Result:** **PASSED**
* **Known Issues:**
  * Deprecated `AppOpsManager.checkOpNoThrow` required for API < 29.
  * BiometricPrompt not available on API 26–27; requires backward-compatible fingerprint dialog.
* **Workaround Implemented:**
  * `AppOpsManager.unsafeCheckOpNoThrow` branched with fallback `checkOpNoThrow` for API < 29 in `PermissionManager.kt`.
  * Jetpack `androidx.biometric:biometric` library used, providing standard AndroidX compatibility back to Android 6.0 with hardware fingerprint fallback.
* **Severity:** Low (Fully compatible).

---

## 3. Subsystem Test Results

| Subsystem | Test Objective | Tested Scenarios | Result |
| :--- | :--- | :--- | :--- |
| **App Monitoring** | Foreground app detection | Normal launch, fast app switching, split-screen, picture-in-picture | **PASS** |
| **Immediate Relock** | Revoke session when exiting | Press Home, press Recents, switch app, screen timeout | **PASS** |
| **Lock Screen Overlay** | Secure overlay on target app | Cold start, warm start, multi-window mode | **PASS** |
| **Biometric Auth** | Fingerprint & Face Unlock | Enrolled fingerprint, failed biometric, fallback to master PIN/Pattern | **PASS** |
| **Media Vault** | Private storage isolation | File encryption, photo/video picker, internal directory protection | **PASS** |
| **Disguise Covers** | Decoy Crash & Calculator | Secret PIN bypass, long-press crash dialog bypass | **PASS** |
| **Uninstall Protection**| Settings interception | Opening device Settings, app info removal attempts | **PASS** |
| **Device Restart** | Boot auto-initialization | Device reboot, service auto-start via `BootReceiver` | **PASS** |
| **Website Blocker** | URL interception in browsers | Chrome, Edge, Firefox, Brave, Opera | **PASS** |
| **Theme System** | Rendering & Contrast | Dark Theme, Light Theme, custom themes, status/nav bar insets | **PASS** |

---

## 4. Conclusion & Readiness

AppLock demonstrates robust stability and performance across the Android ecosystem. All OEM background-termination risks have been mitigated through proactive vendor intent shortcuts and battery optimization exemption pathways.

**Phase 26 status: COMPLETED & APPROVED.**
