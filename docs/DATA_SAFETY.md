# Google Play Console — Data Safety Form Guide (Phase 27)

**Application:** AppLock Privacy Guard (`com.applock.privacy`)  
**Target:** Google Play Console Data Safety Questionnaire  

---

## 1. Summary of Data Collection & Sharing

| Question | Answer | Details |
| :--- | :--- | :--- |
| **Does your app collect or share any user data?** | **NO** | AppLock does not collect or transmit any user data off the device. All processing is 100% local. |
| **Is all of the user data collected by your app encrypted in transit?** | **N/A** (No data transmitted) | Any in-app update checks use TLS 1.3 / HTTPS. |
| **Do you provide a way for users to request that their data is deleted?** | **YES** | Users can delete all vaulted media, logs, and settings inside the app or by clearing App Storage. |

---

## 2. Detailed Data Type Breakdown

### Location
* **Collected:** No
* **Shared:** No

### Personal Info (Name, Email, Phone, Address)
* **Collected:** No
* **Shared:** No

### Financial Info (Credit card, Bank accounts)
* **Collected:** No
* **Shared:** No

### Health and Fitness
* **Collected:** No
* **Shared:** No

### Messages & SMS
* **Collected:** No
* **Shared:** No
* *Note:* Notification Shield temporarily suppresses status bar popups locally via NotificationListenerService without logging or storing message bodies.

### Photos and Videos
* **Collected:** **NO** (Not collected off-device).
* **Processed Locally:** **YES**
* **Purpose:** **App functionality (Private Media Vault)**. Photos and videos selected by the user are stored in the app's private sandbox folder. They are never collected or sent to any server.

### Audio files
* **Collected:** No
* **Shared:** No

### Files and Docs
* **Collected:** No
* **Shared:** No

### App Activity (App interactions, In-app search, Installed apps)
* **Collected:** **NO** (Not collected off-device).
* **Processed Locally:** **YES**
* **Purpose:** **App functionality & Security**. `PACKAGE_USAGE_STATS` evaluates foreground package names strictly in device memory to trigger the lock screen. `QUERY_ALL_PACKAGES` lists installed apps locally for the user to select lock targets. No analytics or tracking is performed.

### Web Browsing (Web history)
* **Collected:** **NO**
* **Processed Locally:** **YES**
* **Purpose:** Built-in Private Browser destroys history and cookies immediately upon exiting. Website Blocker compares active browser URL to local blacklist in memory.

### App Info and Performance (Crash logs, Diagnostics)
* **Collected:** No
* **Shared:** No

### Device or Other IDs
* **Collected:** No
* **Shared:** No

---

## 3. Play Console Policy Declarations

* **QUERY_ALL_PACKAGES Declaration:**
  * **Core Category:** Privacy and Device Management.
  * **Justification:** AppLock's core user-facing functionality is locking and protecting applications installed on the user's device. The application requires package visibility to display installed applications to the user and allow them to protect selected apps.
* **SYSTEM_ALERT_WINDOW Declaration:**
  * **Justification:** Provides the full-screen secure lock screen overlay displayed immediately when an authorized user attempts to open a protected app.
* **ACCESSIBILITY_SERVICE Declaration:**
  * **Justification:** Optional user-enabled feature "Website Blocker" used strictly to detect active web browser URL bar contents to enforce user-configured domain blocks.
