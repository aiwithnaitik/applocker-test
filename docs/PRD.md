# AppLock — Product Requirements Document

**Document:** `PRD.md`
**Product:** AppLock
**Platform:** Android
**Status:** Pre-development
**Version:** 1.0
**Primary Goal:** Build a reliable Android application locker that allows users to protect selected apps using a PIN, pattern, or biometric authentication.

---

# 1. Product Overview

AppLock is an Android privacy and security utility that allows users to lock selected applications behind an authentication screen.

The core experience is:

```text
User selects an app
        ↓
App is marked as locked
        ↓
User opens the protected app
        ↓
AppLock authentication screen appears
        ↓
User authenticates
        ↓
Protected app becomes accessible
```

The application will initially focus on **reliable app locking and a polished lock-screen experience**.

Additional privacy utilities such as website blocking, private browsing, notification security, and advanced security features will be added progressively.

---

# 2. Product Philosophy

The application should follow these principles:

1. **Security first**
2. **Simple UX**
3. **Fast authentication**
4. **Minimal battery impact**
5. **Clear permission explanations**
6. **No unnecessary permissions**
7. **No misleading privacy claims**
8. **Core app-locking functionality must work before secondary features are developed**
9. **The application must remain usable if optional features are skipped**
10. **Do not implement placeholder functionality as if it were functional**

---

# 3. Target Users

Primary users:

* Users who want to protect personal applications
* Users who share their phone with family/friends
* Users who want additional privacy for messaging/social applications
* Users who want customizable app-lock screens

Potential protected apps include:

* Messaging apps
* Social media apps
* Gallery/photo apps
* Banking/finance apps
* Notes apps
* Email apps
* Other user-selected applications

---

# 4. MVP Scope

The first production milestone must include:

### Required

* First-launch onboarding
* Usage Access permission flow
* App discovery
* App selection
* Lock/unlock state management
* PIN authentication
* Pattern authentication
* PIN confirmation
* Biometric authentication
* Display-over-other-apps permission flow
* Basic background reliability handling
* Battery optimization guidance
* Lock screen
* Basic lock-screen themes
* Home screen
* Settings
* Security settings
* Dark mode
* Basic haptic feedback
* Persistent local configuration

### Not required for MVP

* Website blocker
* Private browser
* Notification security
* Fake app icon
* Intruder selfie
* Loud alarm
* Advanced attempt lockout
* Theme marketplace
* Subscription
* Advanced analytics

These features should be architecturally possible later but should not delay the core MVP.

---

# 5. First Launch / Onboarding

## 5.1 Splash Screen

When the application launches for the first time:

```text
AppLock

[Logo]

Loading...
```

The splash screen should be short and should not artificially delay the user.

---

## 5.2 Why Are You Here?

After the splash screen, optionally ask the user why they are using AppLock.

Example:

```text
Why are you using AppLock?

○ I want more privacy
○ I want to protect personal apps
○ I share my phone with others
○ I want extra security
○ Other

[Skip]
[Continue]
```

### Requirements

* This screen must be skippable.
* The answer must not block any functionality.
* Do not use the answer to restrict features.
* The answer may be stored locally for product analytics in a future version, subject to privacy requirements.

---

# 6. Permission Setup

AppLock will require certain Android capabilities to reliably detect protected-app usage and display its authentication interface.

Permissions must be explained before the user is redirected to Android settings.

---

## 6.1 Usage Access

Show:

```text
Allow Usage Access

AppLock needs Usage Access to detect when
a protected application is opened.

[Allow Permission]
[Skip for Now]
```

The user should then be redirected to the appropriate Android settings page.

### Requirements

* Detect whether Usage Access is enabled.
* Show current permission state.
* Allow the user to return to the application.
* If permission is missing, explain the resulting limitation.
* Do not pretend the app is fully functional if required permission is disabled.

---

## 6.2 Display Over Other Apps

Show:

```text
Display Over Other Apps

AppLock uses this permission to display
the lock screen when a protected app opens.

[Enable]
[Skip for Now]
```

The application must detect whether the permission is enabled.

---

## 6.3 Battery Optimization

Explain:

```text
Improve AppLock Reliability

Android may restrict background activity to
save battery.

Allowing AppLock to run without battery
optimization can improve protection reliability.

[Open Settings]
[Skip]
```

### Important

Do not claim that disabling battery optimization guarantees permanent background execution.

OEM-specific Android behavior must be handled gracefully.

---

# 7. App Selection

After permissions, users can select applications to protect.

Screen:

```text
Choose Apps to Lock

Search apps...

○ Instagram
○ WhatsApp
○ Gallery
○ Messages
○ Chrome

[Continue]
[Skip]
```

Each application should display:

* App icon
* App name
* Current state
* Lock/unlock control

### Requirements

* Discover installed user applications.
* Avoid displaying irrelevant/system packages unless explicitly required.
* Handle applications being installed or uninstalled after setup.
* App selection must persist locally.

---

# 8. Authentication Setup

## 8.1 Choose Authentication Method

Users should be able to configure:

* PIN
* Pattern

Biometrics are an optional additional authentication method.

---

## 8.2 PIN

Allow:

* 4-digit PIN
* 6-digit PIN

Example:

```text
Create PIN

Enter your 4 or 6 digit PIN

● ● ● ●
```

---

## 8.3 Confirm PIN

```text
Confirm PIN

Enter your PIN again

● ● ● ●
```

If the PIN does not match:

```text
PINs don't match.
Please try again.
```

Do not store the raw PIN.

The implementation must use secure credential storage / hashing appropriate for the platform.

---

# 9. Pattern Authentication

Allow the user to configure a pattern.

Requirements:

* Minimum secure pattern length
* Pattern confirmation
* Clear visual feedback
* Optional invisible pattern lines
* Secure local storage of the credential representation

The exact minimum pattern length should be determined during implementation according to Android security best practices.

---

# 10. Biometric Authentication

After PIN/pattern setup:

```text
Use Biometrics?

Unlock protected apps faster with
fingerprint or supported biometrics.

[Enable]
[Not Now]
```

### Requirements

* Use Android's supported biometric authentication APIs.
* Never store biometric data.
* The application must always retain a fallback authentication method.
* Users must be able to disable biometrics later.

---

# 11. Completion Screen

Example:

```text
You're All Set

Your protected apps are now ready.

You can manage locked apps anytime
from the home screen.

[Go to Home]
```

Do not claim that apps are protected if required Android permissions are still missing.

---

# 12. Home Screen

The home screen is the primary control center.

Recommended structure:

```text
AppLock

[ Locked ] [ Unlocked ]

────────────────────

Instagram       🔒
WhatsApp        🔒
Gallery         🔒

────────────────────

YouTube         🔓
Chrome          🔓
Spotify         🔓
```

Users can switch between:

* Locked
* Unlocked

The home screen must make the current state immediately understandable.

---

# 13. Lock / Unlock Interaction

Each application row should have a lock-state control.

Example:

```text
Instagram                         🔒
```

When the user taps the control:

```text
Instagram                         🔓
```

The application moves between the locked and unlocked sections.

Requirements:

* State changes should be immediate.
* State must persist after app restart.
* UI must update without requiring a manual refresh.
* Uninstalled applications must not remain as invalid entries.

---

# 14. Core App-Locking Engine

This is the most important subsystem.

When a protected application is opened:

```text
Protected App Detected
        ↓
Check authentication state
        ↓
Already authenticated?
     /          \
   YES           NO
    ↓             ↓
Open app      Show lock screen
                  ↓
             Authenticate
                  ↓
             Allow access
```

### Requirements

* Detect protected-app launches.
* Display authentication when necessary.
* Avoid locking AppLock itself unintentionally.
* Avoid authentication loops.
* Handle app switching.
* Handle returning from recent apps.
* Handle device restart.
* Handle screen off/on.
* Handle protected app reopening.
* Handle incorrect authentication.
* Handle permission revocation.

### Reliability is more important than animation.

---

# 15. Lock Screen

The lock screen should contain:

```text
Instagram

Enter PIN

● ● ● ●

1 2 3
4 5 6
7 8 9
  0

[Use Biometrics]
```

For pattern mode:

```text
Instagram

Draw your pattern

[Pattern]
```

### Requirements

* Show protected application's identity where appropriate.
* Support configured authentication method.
* Support biometric fallback.
* Provide incorrect-authentication feedback.
* Respect configured theme.
* Support dark mode.

---

# 16. Themes

Themes customize the authentication screen.

## 16.1 Standard Themes

Users can customize:

* Background color
* PIN background color
* PIN text color
* Pattern color
* General accent color

Basic color customization is free.

---

# 17. Image Themes

Image-based themes can include:

* Predefined backgrounds
* Curated designs
* Seasonal designs
* Other visual themes

Potential monetization:

```text
Unlock Theme

Watch an ad — FREE

OR

Purchase — ₹9
```

The exact pricing must remain configurable and should not be hardcoded throughout the application.

---

# 18. Custom Themes

Users can create their own themes.

Example:

```text
Create Theme

Background Image
[Choose Image]

PIN Background
[Color]

PIN Text
[Color]

Pattern
[Color]

Preview

[Save]
```

After saving:

```text
Watch a short ad to unlock
```

After the rewarded ad completes successfully:

* Save the theme
* Add it to My Themes
* Apply it automatically

Do not apply the theme before successful ad completion.

---

# 19. My Themes

All acquired themes should appear in:

```text
My Themes
```

Possible categories:

* Free
* Purchased
* Ad Unlocked
* Custom

Users can:

* Preview
* Apply
* Delete custom themes
* Set active theme

---

# 20. Tools Screen

The Tools screen will contain additional privacy utilities.

Initial UI:

```text
Tools

Website Blocker
Coming Soon

Private Browser
Coming Soon

Notification Security
Coming Soon
```

These should clearly indicate that they are not yet available.

Do not implement fake interactions.

---

# 21. Website Blocker — Future Feature

Users will eventually be able to specify websites to block.

Example:

```text
Blocked Websites

instagram.com        ON
example.com           ON

[+ Add Website]
```

Potential use cases:

* Distraction blocking
* Privacy
* User-defined website restrictions

### Important

Do not promise universal blocking across every browser unless the implementation actually supports it.

Cross-browser website blocking should be researched separately before implementation.

---

# 22. Private Browser — Future Feature

AppLock may eventually contain a private browsing environment.

Requirements:

* Browser inside AppLock
* Session-based browsing
* Clear locally stored browsing data when the session ends
* No misleading claim of anonymity
* Clear privacy disclosure

Private browsing must not be marketed as making the user anonymous or invisible to websites/network providers.

---

# 23. Notification Security — Future Feature

Users may enable:

```text
Notification Security
ON
```

For protected applications, notification content should be hidden according to the capabilities and restrictions of the Android notification system.

Example:

Instead of:

```text
WhatsApp
Rahul: Where are you?
```

Show:

```text
WhatsApp
New notification
```

Exact behavior must be implemented according to Android notification APIs and platform restrictions.

---

# 24. Settings

Settings should be organized into logical sections.

---

## 24.1 Security

* Change PIN
* Change pattern
* Enable/disable biometrics
* Auto-lock behavior
* Incorrect-attempt protection
* Intruder detection
* Attempt history

---

## 24.2 Lock Screen

* Current theme
* My Themes
* Unlock animation
* PIN background color
* PIN text color
* Pattern visibility
* Vibrate on touch
* Vibrate on incorrect authentication

---

## 24.3 Privacy

* Notification Security
* Website Blocker
* Private Browser

---

## 24.4 Appearance

* Dark mode
* Theme
* Accent color

---

## 24.5 Advanced

* Fake app appearance
* Background behavior
* Troubleshooting
* Permission status

---

# 25. Customization

Free customization:

* Unlock animation
* Vibrate on touch
* Dark mode
* Vibrate on incorrect PIN
* Basic color customization

Potential Pro customization:

* Advanced animations
* Invisible pattern lines
* Advanced fingerprint presentation
* Additional lock-screen customization

Premium functionality must never be required for the core app-locking functionality.

---

# 26. Intruder Detection — Future Feature

When enabled:

```text
Incorrect attempts:
3 / 5 / 10
```

After the configured number of incorrect authentication attempts:

* Trigger configured security response.
* Optionally capture a photograph using the device camera.
* Store the result locally.
* Record timestamp.
* Display the event in security history.

### Privacy requirements

* Camera permission must be requested only when this feature is enabled.
* Explain exactly what the camera will be used for.
* Photos should remain local unless the user explicitly chooses otherwise.
* Do not secretly upload photographs.

---

# 27. Intruder Alarm — Future Feature

Optional security response:

```text
Intruder Alarm
ON / OFF

Trigger:
3 / 5 / 10 failed attempts
```

The implementation must provide a reliable way for the legitimate user to stop the alarm after successful authentication.

The alarm must not create an unrecoverable state.

---

# 28. Attempt Lockout

Future feature.

Example progression:

```text
Failed attempts

1st event → 1 minute
2nd event → 5 minutes
3rd event → 10 minutes
```

The exact algorithm should be configurable.

Requirements:

* Persist lockout state.
* Display remaining time.
* Reset according to clearly defined rules.
* Prevent accidental permanent lockout.

---

# 29. Fake App Appearance — Future Feature

Potential app appearance options:

* Calculator
* Weather
* Clock
* Music
* Notes
* Other supported appearances

This feature requires additional investigation into Android launcher behavior and Google Play policies before implementation.

Do not implement this feature until those constraints are verified.

---

# 30. Data Storage

The application should store configuration locally.

Potential local data:

```text
User Settings
Protected Apps
Authentication Configuration
Theme Configuration
Custom Themes
Security Events
Attempt History
```

### Security

Never store:

* Raw PIN
* Raw authentication credentials
* Biometric information

Use secure Android storage mechanisms for sensitive configuration.

---

# 31. Permissions

Only request permissions required by implemented functionality.

Potential permissions/capabilities include:

* Usage Access
* Display over other apps
* Biometric authentication
* Camera — only for intruder detection
* Notification-related access — only if notification security requires it
* Battery optimization exemption guidance

Permissions should be:

1. Explained
2. Requested at the appropriate time
3. Checked before use
4. Recoverable if revoked

---

# 32. Error Handling

The application must gracefully handle:

* Permission denied
* Permission revoked
* App uninstalled
* Protected app unavailable
* Biometric unavailable
* Biometric failure
* Incorrect PIN
* Incorrect pattern
* Device restart
* Background restrictions
* Android version differences
* OEM-specific behavior

Never crash because a permission is missing.

---

# 33. Security Requirements

Security is a core product requirement.

The application must:

* Never store plaintext PINs.
* Never store biometric data.
* Use Android security APIs wherever possible.
* Keep sensitive data local by default.
* Avoid unnecessary network communication.
* Clearly disclose optional data collection.
* Protect internal configuration from casual tampering.
* Handle authentication failures safely.
* Avoid exposing sensitive information in logs.

---

# 34. Performance Requirements

The application should:

* Launch quickly.
* Consume minimal battery.
* Avoid unnecessary background work.
* Avoid excessive memory usage.
* Keep authentication response fast.
* Avoid noticeable delays when opening protected applications.

The locking mechanism should prioritize reliability without continuously polling at unnecessarily high frequency.

---

# 35. Monetization

## Free

Core functionality remains free:

* App locking
* PIN
* Pattern
* Biometrics
* Basic themes
* Color customization
* Basic settings
* Basic security

## Ads

Use rewarded ads primarily for:

* Unlocking certain image themes
* Unlocking custom themes

Avoid intrusive ads immediately before authentication.

---

# 36. Theme Purchase

Potential paid theme:

```text
₹9
```

The price must be configurable.

Purchased themes become permanently available under:

**My Themes**

Do not hardcode ₹9 into business logic.

---

# 37. Pro Subscription

Potential Pro functionality:

* Advanced security
* Intruder detection
* Advanced customization
* Premium lock-screen functionality
* Advanced themes
* Additional security controls

The subscription price is **not finalized** and must be configurable.

Do not implement billing until the exact premium feature set and pricing strategy are finalized.

---

# 38. Analytics

Analytics should be privacy-conscious.

Potential non-sensitive product events:

```text
onboarding_completed
app_lock_enabled
theme_applied
theme_created
rewarded_ad_completed
settings_opened
```

Do not collect sensitive user data unnecessarily.

Analytics should be designed so that the application remains functional if analytics are unavailable.

---

# 39. Architecture Requirements

The codebase should be modular.

Recommended modules:

```text
app/
├── onboarding/
├── permissions/
├── apps/
├── locker/
├── authentication/
├── biometrics/
├── themes/
├── settings/
├── security/
├── tools/
├── ads/
├── billing/
└── storage/
```

The exact project structure may change based on the selected Android technology, but responsibilities must remain separated.

---

# 40. State Management

The following state must be persistent:

* Onboarding completion
* Permission status
* Protected applications
* Authentication method
* Theme selection
* User settings
* Security settings
* Custom themes

Temporary runtime state should not be unnecessarily persisted.

---

# 41. Navigation

Primary navigation:

```text
Home
Tools
Themes
Settings
```

The exact navigation pattern may be:

* Bottom navigation
* Navigation rail
* Another Android-native pattern

Choose based on screen size and Material Design guidelines.

---

# 42. Design Direction

The application should feel:

* Modern
* Minimal
* Secure
* Fast
* Clean
* Premium without being visually complicated

Avoid:

* Excessive gradients
* Excessive animations
* Cluttered dashboards
* Unnecessary cards
* Intrusive advertisements
* Fake security claims

Use consistent:

* Typography
* Spacing
* Iconography
* Corner radius
* Button styles
* Color tokens

---

# 43. MVP User Journey

The complete MVP journey should be:

```text
Install
  ↓
Splash
  ↓
Optional "Why AppLock?"
  ↓
Usage Access
  ↓
Select Apps
  ↓
Set PIN/Pattern
  ↓
Confirm
  ↓
Biometrics
  ↓
Overlay Permission
  ↓
Battery Optimization Guidance
  ↓
Finish
  ↓
Home
  ↓
Select Instagram
  ↓
Instagram becomes LOCKED
  ↓
Open Instagram
  ↓
AppLock screen appears
  ↓
Enter PIN / Pattern / Biometrics
  ↓
Instagram opens
```

This journey must work reliably before expanding the product.

---

# 44. MVP Acceptance Criteria

The MVP is considered functional when:

### Installation

* App installs successfully.
* App launches without crashing.

### Onboarding

* User can complete onboarding.
* User can skip optional steps.
* Permissions are correctly detected.

### App Management

* Installed applications can be discovered.
* User can select applications.
* Selected apps persist as protected apps.
* User can lock/unlock applications.

### Authentication

* PIN works.
* Pattern works.
* PIN confirmation works.
* Biometrics work where supported.
* Incorrect credentials are rejected.

### Locking

* Protected application launches trigger authentication.
* Correct authentication allows access.
* Incorrect authentication prevents access.
* State survives app restart.
* State survives device restart where technically supported.

### Themes

* Basic color customization works.
* Selected theme persists.
* Lock screen reflects selected theme.

### Settings

* User can change authentication settings.
* User can enable/disable biometrics.
* User can modify supported customization settings.

---

# 45. Development Priority

Implement in this order:

## Phase 1 — Foundation

* Android project
* Architecture
* Navigation
* Design system
* Local storage
* Basic settings

## Phase 2 — Onboarding

* Splash
* Why AppLock
* Permission screens
* Usage Access
* Overlay permission
* Battery optimization guidance

## Phase 3 — App Management

* Installed app discovery
* Search
* App selection
* Locked/unlocked state
* Persistence

## Phase 4 — Authentication

* PIN
* Pattern
* Confirmation
* Biometrics

## Phase 5 — Lock Engine

* Detect protected app
* Show lock screen
* Authenticate
* Release protected app
* Handle lifecycle edge cases

## Phase 6 — Themes

* Standard themes
* Color customization
* Theme persistence
* My Themes

## Phase 7 — Settings

* Security
* Authentication
* Appearance
* Lock-screen customization

## Phase 8 — Testing

Test across:

* Different Android versions
* Different screen sizes
* Device restart
* Permission revocation
* Background restrictions
* App switching
* Recent apps
* Screen lock/unlock
* Incorrect authentication
* Biometric failure

## Phase 9 — Monetization

Only after the core experience is stable:

* Rewarded ads
* Theme purchases
* Pro subscription

## Phase 10 — Future Features

* Notification Security
* Website Blocker
* Private Browser
* Intruder Detection
* Alarm
* Attempt Lockout
* Fake App Appearance
* Smart Themes

---

# 46. Critical Development Rule

**Do not implement future features while the core locking engine is unstable.**

The development agent must follow this priority:

```text
RELIABILITY
    ↓
SECURITY
    ↓
CORE UX
    ↓
CUSTOMIZATION
    ↓
MONETIZATION
    ↓
ADDITIONAL FEATURES
```

A beautiful AppLock that occasionally fails to lock an application is not acceptable.

---

# 47. Definition of V1

V1 means:

> A user can install AppLock, configure authentication, select applications, lock them, reliably encounter the AppLock authentication screen when opening protected applications, authenticate, and manage their protected applications from a polished home screen.

Everything else is secondary.

---

# 48. Future Product Direction

The long-term product can evolve from:

**App Locker**

into:

**Privacy Toolkit**

with:

```text
App Protection
      +
Lock Screen Customization
      +
Notification Privacy
      +
Website Blocking
      +
Private Browser
      +
Security Events
      +
Advanced Protection
```

However, every new feature must be evaluated independently for:

* Android API feasibility
* Privacy implications
* Battery impact
* Security implications
* Google Play policy compliance
* Maintenance cost
* User value

```

This is the PRD I'd give to the coding agent **before letting it write production code**. The most important instruction is the Phase 5 **lock engine**: get that working reliably first, because almost every other feature depends on it.
```
