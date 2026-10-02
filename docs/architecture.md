# AppLock — Architecture Document

**Project:** AppLock
**Platform:** Android
**Architecture Version:** 1.0
**Status:** Pre-development
**Primary Product Specification:** `PRD.md`
**Development Sequence:** `phases.md`

---

# 1. Purpose

This document defines the technical architecture of AppLock.

It is the technical source of truth for:

* Programming language
* Android framework
* UI framework
* Application architecture
* Module boundaries
* Data storage
* Security architecture
* Android system integrations
* Authentication
* App-monitoring strategy
* Theme system
* Future extensibility

The coding agent MUST use this document together with `PRD.md`.

If a technical implementation decision conflicts with this document, the agent must explain the conflict before changing the architecture.

---

# 2. Architecture Decision

AppLock will be built as a **native Android application**.

The primary technology stack is:

```text
Language
    Kotlin

UI
    Jetpack Compose

Architecture
    MVVM + layered architecture

Async / Reactive
    Kotlin Coroutines
    Kotlin Flow

Navigation
    Navigation Compose

Local Preferences
    DataStore

Structured Local Database
    Room

Authentication
    Android BiometricPrompt
    Secure local credential storage

Android System Integration
    Native Android APIs

Build System
    Gradle
    GitHub Actions (Cloud CI/CD for APK generation & testing on physical devices)

IDE / Editor Environment
    Lightweight Editor (Antigravity IDE / VS Code)
    Android Studio (Optional / Not required for compilation)
```

---

# 3. Why Native Android

AppLock is fundamentally an Android system-integration application.

Its most important functionality depends on Android-specific capabilities:

* Detecting application usage
* Determining which application is currently active
* Displaying an authentication interface when necessary
* Biometric authentication
* Overlay/system UI behavior
* Background execution
* Package/application discovery
* Permission management
* Camera access
* Notification-related functionality
* Battery optimization behavior

Therefore, the application should not be built as a web application wrapped inside Android.

---

# 4. Why Kotlin

Kotlin is the primary language.

Reasons:

* First-class Android support
* Strong interoperability with Android APIs
* Null-safety
* Coroutines
* Modern Android development support
* Excellent Jetpack integration
* Suitable for security-sensitive application code
* Large Android ecosystem

All native Android functionality should be implemented in Kotlin.

---

# 5. Why Jetpack Compose

The UI will use Jetpack Compose rather than XML-based Android Views for new screens.

Compose will be responsible for:

* Onboarding
* Home screen
* Tools screen
* Themes
* Settings
* Authentication UI
* Lock screen
* Dialogs
* Permission explanations
* Custom theme editor
* Reusable UI components

Compose should not be responsible for system-level app monitoring.

---

# 6. High-Level Architecture

The application follows a layered architecture:

```text
┌─────────────────────────────────────────┐
│              Presentation               │
│                                         │
│ Jetpack Compose                         │
│ Screens                                 │
│ Components                              │
│ ViewModels                              │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│                 Domain                  │
│                                         │
│ Use Cases                               │
│ Business Rules                          │
│ Models                                  │
│ Security Rules                          │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│                  Data                   │
│                                         │
│ Repositories                            │
│ DataStore                               │
│ Room                                    │
│ Secure Storage                          │
└────────────────────┬────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────┐
│          Android System Layer           │
│                                         │
│ UsageStatsManager                       │
│ PackageManager                          │
│ BiometricPrompt                         │
│ Overlay APIs                            │
│ Camera APIs                             │
│ Permission APIs                          │
│ Battery APIs                            │
└─────────────────────────────────────────┘
```

---

# 7. Architectural Principle

The UI must not directly control Android system APIs.

Incorrect:

```text
Compose Screen
      ↓
UsageStatsManager
```

Preferred:

```text
Compose Screen
      ↓
ViewModel
      ↓
Use Case
      ↓
Repository / System Service
      ↓
Android API
```

This keeps the application testable and prevents Android-specific logic from spreading throughout the UI.

---

# 8. Project Structure

The initial project should use a modular package structure.

Recommended:

```text
app/
└── src/
    └── main/
        └── java/
            └── <package>/
                │
                ├── AppLockApplication.kt
                ├── MainActivity.kt
                │
                ├── core/
                │   ├── common/
                │   ├── security/
                │   ├── storage/
                │   └── ui/
                │
                ├── data/
                │   ├── local/
                │   ├── repository/
                │   └── model/
                │
                ├── domain/
                │   ├── model/
                │   ├── repository/
                │   └── usecase/
                │
                ├── feature/
                │   ├── onboarding/
                │   ├── home/
                │   ├── apps/
                │   ├── authentication/
                │   ├── lockscreen/
                │   ├── themes/
                │   ├── settings/
                │   └── tools/
                │
                └── system/
                    ├── appmonitor/
                    ├── permissions/
                    ├── usageaccess/
                    ├── overlay/
                    ├── biometrics/
                    ├── battery/
                    └── camera/
```

The exact package names may be changed if required, but the separation of responsibilities should remain.

---

# 9. Core Module

The `core` layer contains functionality shared by multiple features.

Example:

```text
core/
├── common/
├── security/
├── storage/
└── ui/
```

## core/common

Contains:

* Result wrappers
* Error types
* Utility functions
* Constants
* Common extensions

Do not put feature-specific business logic here.

---

# 10. Core UI

`core/ui` contains reusable Compose components.

Examples:

```text
AppButton
AppCard
AppTopBar
AppDialog
AppTextField
AppToggle
AppIcon
PermissionCard
AppSectionHeader
LoadingView
ErrorView
```

Design tokens should also be centralized:

```text
Colors
Typography
Spacing
Shapes
Elevation
Animation specifications
```

Avoid duplicating UI constants across screens.

---

# 11. Domain Layer

The domain layer contains application logic independent of UI implementation.

Example use cases:

```text
GetInstalledApps
GetProtectedApps
ProtectApp
UnprotectApp
CreatePin
ValidatePin
CreatePattern
ValidatePattern
AuthenticateWithBiometric
GetPermissionStatus
ApplyTheme
CreateCustomTheme
```

The domain layer should not import Compose UI classes.

---

# 12. Data Layer

The data layer is responsible for persistence and repositories.

Structure:

```text
data/
├── local/
├── model/
└── repository/
```

Repositories provide a stable interface to the domain layer.

Example:

```text
ProtectedAppRepository
AuthenticationRepository
ThemeRepository
SettingsRepository
SecurityEventRepository
```

The UI should not directly query Room or DataStore.

---

# 13. Local Storage Strategy

AppLock does not require a backend server for core functionality.

The initial application should be **local-first**.

Data is stored on the device.

---

# 14. DataStore

Use Android DataStore for small preference/configuration data.

Examples:

```text
Onboarding completed
Selected authentication method
Biometric enabled
Dark mode enabled
Vibration enabled
Current theme ID
Battery optimization guidance state
Other simple settings
```

DataStore should not be used as a replacement for a relational database.

---

# 15. Room

Use Room for structured data.

Potential entities:

```text
ProtectedApp
Theme
CustomTheme
SecurityEvent
AttemptRecord
```

Example:

```text
ProtectedApp
-------------------------
packageName
displayName
isLocked
createdAt
updatedAt
```

The exact schema should be finalized during implementation.

---

# 16. Sensitive Credential Storage

Authentication credentials are security-sensitive.

The application MUST NOT store:

```text
Plaintext PIN
Plaintext pattern
```

Use appropriate Android cryptographic/security mechanisms.

Where applicable, Android Keystore should be used to protect encryption keys or other sensitive cryptographic material.

The coding agent must research the current recommended Android approach before implementing credential storage.

---

# 17. Authentication Architecture

Authentication consists of three independent mechanisms:

```text
PIN
Pattern
Biometric
```

Architecture:

```text
Authentication UI
       ↓
Authentication ViewModel
       ↓
Authentication Use Case
       ↓
Authentication Repository
       ↓
Secure Credential Storage
```

Biometric authentication:

```text
Authentication UI
       ↓
Biometric Use Case
       ↓
Android BiometricPrompt
```

Biometric data itself is never accessed or stored by AppLock.

---

# 18. App Discovery

AppLock needs to identify applications installed on the device.

The system layer will use Android's application/package APIs.

Primary responsibility:

```text
PackageManager
       ↓
Installed Applications
       ↓
AppInfo Model
       ↓
Repository
       ↓
UI
```

The application should distinguish between:

* User applications
* System applications

The initial MVP should prioritize normal user-installed applications.

---

# 19. Protected Application Model

A protected application should be identified by its Android package name.

Example:

```text
com.example.application
```

Do not identify applications solely by display name.

Display names can change or collide.

The package name is the canonical application identifier.

---

# 20. Core App-Locking Architecture

This is the most critical subsystem.

The architecture is:

```text
Android System
      ↓
Application Usage Detection
      ↓
App Monitor
      ↓
Protected App Repository
      ↓
Is application protected?
      ↓
YES
      ↓
Lock State Manager
      ↓
Authentication Required?
      ↓
YES
      ↓
Lock Screen
      ↓
Authentication
      ↓
Success
      ↓
Allow access
```

---

# 21. App Monitoring

The `system/appmonitor` module is responsible for determining when a protected application becomes active.

Potential Android APIs include:

* UsageStatsManager
* Usage Events
* Other officially supported Android mechanisms where appropriate

The implementation must be validated against the minimum supported Android version.

Do not assume one API works identically across all Android versions.

---

# 22. App Monitoring Rules

The app monitor must:

* Detect protected application transitions.
* Avoid unnecessary polling.
* Minimize battery consumption.
* Avoid locking unrelated applications.
* Avoid repeatedly triggering the lock screen for the same authentication session.
* Handle app switching.
* Handle screen lock/unlock.
* Handle device restart.
* Handle permission removal.

The implementation should prioritize correctness over aggressive monitoring frequency.

---

# 23. Lock State Manager

The lock state manager determines whether an authentication screen is required.

Example state:

```text
Protected App
      ↓
Authentication Session
      ↓
Valid?
  /       \
YES       NO
 ↓         ↓
Allow    Lock
```

The lock state manager must not be tied directly to a specific Compose screen.

It should expose state that the presentation layer can consume.

---

# 24. Authentication Session

After successful authentication, AppLock may maintain a temporary authentication session depending on the configured security behavior.

The exact session behavior should be configurable later.

Possible policies:

```text
Authenticate every launch

Authenticate after leaving protected app

Authenticate after timeout

Authenticate after device lock
```

The initial MVP should implement the simplest reliable behavior.

Do not add complicated session policies before the basic lock mechanism is stable.

---

# 25. Lock Screen Architecture

The lock screen is a Compose-based UI.

It receives:

```text
Protected application information
Authentication method
Current theme
Authentication state
```

It should provide:

```text
PIN keypad
Pattern input
Biometric trigger
Error state
Loading state
```

The lock screen must not contain the actual credential-validation algorithm.

---

# 26. Overlay / System UI

AppLock may require Android mechanisms capable of presenting its authentication UI when a protected application is active.

This functionality belongs to:

```text
system/overlay/
```

The exact mechanism must be validated against:

* Android version
* Device manufacturer
* Permission requirements
* Google Play policies
* Lifecycle behavior

Do not implement a workaround that violates Android security boundaries.

---

# 27. Permission Architecture

Permissions should be abstracted behind a permission manager.

Example:

```text
PermissionManager

isUsageAccessGranted()
isOverlayGranted()
isBatteryOptimizationDisabled()
isCameraAvailable()
```

Feature-specific permission requirements should not be scattered across UI screens.

---

# 28. Permission State Flow

```text
Permission UI
      ↓
Permission ViewModel
      ↓
Permission Use Case
      ↓
Permission Manager
      ↓
Android System
```

After returning from Android settings, the application must re-check the actual permission state.

Never assume that opening the settings screen means the permission was granted.

---

# 29. Biometric Architecture

Use:

```text
AndroidX Biometric
```

with Android's supported biometric authentication mechanism.

Flow:

```text
User taps biometric button
        ↓
Check availability
        ↓
Launch BiometricPrompt
        ↓
Android authenticates user
        ↓
Success / Failure
```

AppLock does not receive fingerprint data.

---

# 30. Theme Architecture

Themes should be data-driven.

A theme should contain properties such as:

```text
Theme
├── id
├── name
├── type
├── background
├── pinBackground
├── pinTextColor
├── patternColor
├── accentColor
├── imageReference
└── metadata
```

Theme types:

```text
STANDARD
IMAGE
CUSTOM
PREMIUM
```

The exact model may evolve.

---

# 31. Theme Rendering

The lock screen should consume a theme object:

```text
LockScreen
      ↓
ThemeState
      ↓
Theme Renderer
      ↓
Compose UI
```

The lock screen must not contain hardcoded theme-specific layouts.

This allows new themes to be added without rewriting the lock screen.

---

# 32. Custom Theme Architecture

Custom themes consist of:

```text
Background Image
PIN Background Color
PIN Text Color
Pattern Color
Accent Color
```

The selected image should be referenced through a safe local URI/storage mechanism rather than unnecessarily duplicating large image data inside the database.

---

# 33. Theme Entitlements

Themes may have different entitlement states:

```text
FREE
PURCHASED
REWARDED_AD
CUSTOM
PREMIUM
```

The theme system should determine whether a user owns/unlocked a theme.

Do not hardcode monetization checks directly inside the Compose UI.

Preferred:

```text
Theme UI
   ↓
Theme Entitlement Use Case
   ↓
Entitlement Repository
```

---

# 34. Monetization Architecture

Monetization will be added after the core application is stable.

Possible systems:

```text
Ads
Billing
Theme Entitlements
Subscription Entitlements
```

These should be isolated from core security functionality.

The AppLock engine must continue to work if:

* Ads fail
* Billing is unavailable
* Internet is unavailable
* Play services are temporarily unavailable

---

# 35. Rewarded Ads

Rewarded ads may be used to unlock selected themes.

Flow:

```text
User selects locked theme
        ↓
Watch Ad
        ↓
Ad completed successfully?
      /        \
    YES         NO
     ↓           ↓
Unlock        Remain locked
```

The application must not grant the reward merely because an ad was started.

---

# 36. Billing

Billing should be isolated behind a billing abstraction.

Example:

```text
BillingRepository
```

The rest of the application should not directly depend on Google Play Billing implementation details.

Potential future products:

```text
Theme purchase
Pro subscription
```

Product IDs must be configurable and centralized.

---

# 37. Tools Architecture

Future tools should be independent modules.

```text
tools/
├── websiteblocker/
├── privatebrowser/
└── notificationsecurity/
```

Each tool must have:

* Its own feature UI
* Its own domain logic
* Its own Android integrations
* Its own permission requirements

Do not place tool-specific logic inside the core AppLock engine.

---

# 38. Website Blocker Architecture

Future architecture:

```text
Website Blocker UI
       ↓
Website Repository
       ↓
Blocking Engine
       ↓
Android-supported network/browser mechanism
```

The exact implementation must be researched separately.

The architecture must not assume that AppLock can universally block every website in every browser.

---

# 39. Private Browser Architecture

The private browser will be an independent feature.

Potential structure:

```text
Private Browser UI
       ↓
Browser Session Manager
       ↓
WebView / supported browser technology
       ↓
Session Storage
```

Session data should be cleared according to the product's privacy requirements.

The feature must not make claims of complete anonymity.

---

# 40. Notification Security Architecture

Future notification protection will be isolated in:

```text
system/notifications/
```

Potential Android APIs and restrictions must be evaluated before implementation.

The application should only request notification-related permissions if the feature is enabled.

---

# 41. Intruder Detection Architecture

Future feature:

```text
Failed Authentication
        ↓
Attempt Counter
        ↓
Threshold Reached?
        ↓
Intruder Detection
        ↓
Camera
        ↓
Local Security Event
```

The camera must not activate silently without the user enabling the feature and granting the required permission.

---

# 42. Security Event Model

Future security events may include:

```text
FAILED_AUTHENTICATION
INTRUDER_DETECTED
LOCKOUT_STARTED
LOCKOUT_ENDED
```

Potential fields:

```text
id
type
timestamp
attemptCount
metadata
```

Sensitive event data should remain local by default.

---

# 43. Background Execution

Background behavior is one of the highest-risk areas of this application.

The implementation must account for:

* Android background restrictions
* Doze mode
* Battery optimization
* OEM-specific process management
* Application lifecycle
* Device restart
* Permission revocation

Do not assume that a continuously running background service is always appropriate.

Use the minimum background mechanism necessary to achieve the required functionality.

---

# 44. Battery Requirements

AppLock should minimize battery consumption.

Do not implement:

```text
Infinite tight polling loop
```

or equivalent high-frequency background work.

Monitoring frequency and architecture must be selected based on Android-supported mechanisms.

The agent must measure/consider battery impact during testing.

---

# 45. Lifecycle Handling

The application must account for:

```text
App launch
App background
App foreground
Activity recreation
Configuration change
Screen off
Screen on
Device restart
Process termination
Permission changes
```

Important security state should be persisted appropriately rather than relying entirely on in-memory state.

---

# 46. Error Handling Architecture

Use structured error handling.

Example:

```text
sealed class AppError
```

Potential categories:

```text
PermissionDenied
AuthenticationFailed
BiometricUnavailable
StorageError
SystemApiUnavailable
AppNotFound
UnknownError
```

UI should display user-friendly messages.

Raw exceptions should not be shown directly to users.

---

# 47. Logging

Use structured logging during development.

Logs must not contain:

* PINs
* Authentication credentials
* Sensitive personal data
* Private images
* Security secrets

Debug logging should be disabled/reduced in production builds.

---

# 48. Dependency Management

Dependencies must be kept minimal.

The coding agent must:

1. Prefer official Android/Jetpack libraries.
2. Avoid unnecessary third-party libraries.
3. Check whether a feature can be implemented using Android APIs before adding a dependency.
4. Explain the purpose of every major dependency.
5. Avoid libraries that are abandoned or poorly maintained.
6. Avoid adding a dependency solely for convenience when native APIs are sufficient.

---

# 49. Networking

The core AppLock functionality does not require a backend.

The MVP should function without an internet connection.

Internet may later be required for:

* Rewarded ads
* Billing
* Remote theme catalog
* Optional analytics

Security functionality must not depend on network connectivity.

---

# 50. Backend

There is **no backend server in the initial architecture**.

Do not introduce:

* FastAPI
* Node.js
* Firebase backend
* PostgreSQL
* Remote authentication

unless a future product requirement explicitly requires them.

---

# 51. Privacy Architecture

The application follows a local-first privacy model.

Default behavior:

```text
User data
   ↓
Device
   ↓
Local storage
```

The application should not upload:

* PINs
* Patterns
* Intruder photographs
* Protected-app lists
* Private browsing data

unless a future feature explicitly requires remote storage and the user clearly consents.

---

# 52. Security Boundary

The following components are security-sensitive:

```text
Authentication
Credential Storage
App Monitor
Lock State
Intruder Detection
Security Events
Permission Management
```

These components require stronger review than ordinary UI components.

The coding agent must not casually refactor security-sensitive code without explaining the consequences.

---

# 53. UI/Data Separation

The following is prohibited:

```text
Composable
   ↓
Direct database query
```

Preferred:

```text
Composable
   ↓
ViewModel
   ↓
Use Case
   ↓
Repository
   ↓
Database
```

Similarly:

```text
Composable
   ↓
ViewModel
   ↓
Use Case
   ↓
System Service
   ↓
Android API
```

---

# 54. State Management

Use a unidirectional data-flow approach where practical.

Example:

```text
UI Event
   ↓
ViewModel
   ↓
Use Case
   ↓
State Update
   ↓
UI
```

Example:

```text
User taps "Lock"
        ↓
LockApp event
        ↓
ViewModel
        ↓
ProtectAppUseCase
        ↓
Repository
        ↓
Database
        ↓
Updated state
        ↓
Compose UI
```

---

# 55. ViewModel Responsibilities

ViewModels should:

* Hold UI state
* Handle UI events
* Call use cases
* Expose state through Flow/StateFlow
* Handle lifecycle-safe asynchronous work

ViewModels should not:

* Contain massive business logic
* Directly manipulate database implementations
* Directly implement Android security mechanisms
* Become giant "god classes"

---

# 56. Repository Responsibilities

Repositories abstract data sources.

Example:

```text
ProtectedAppRepository
```

may internally use:

```text
Room
PackageManager
DataStore
```

The domain layer should not need to know how the data is retrieved.

---

# 57. Testing Architecture

Testing should exist at multiple levels.

## Unit Tests

Test:

* PIN validation
* Pattern validation
* Theme logic
* Lock-state logic
* Attempt counters
* Entitlement logic
* Use cases

## Integration Tests

Test:

* Database
* DataStore
* Repository behavior
* Authentication flows where practical

## UI Tests

Test:

* Onboarding
* Navigation
* App selection
* Authentication UI
* Theme selection
* Settings

## Device Tests

Critical for AppLock:

* App switching
* Opening protected applications
* Screen locking/unlocking
* Recent apps
* Device restart
* Permission changes
* Background behavior

---

# 58. Core Technical Risk

The biggest technical risk is **reliable foreground-app detection and lock-screen presentation across Android versions and manufacturers**.

Therefore:

> The app-monitoring and locking mechanism must be validated experimentally before the architecture is considered fully proven.

The coding agent must not claim universal compatibility without testing.

---

# 59. Android Compatibility

The project should define:

```text
Minimum Android version
Target Android version
Compile SDK
```

during Phase 0 after evaluating the APIs required by the app-lock mechanism.

The agent must not arbitrarily choose these values without considering:

* Usage Access availability
* Overlay behavior
* Biometric APIs
* Background execution
* Package visibility
* Security restrictions
* Google Play requirements

---

# 60. OEM Compatibility

Testing should eventually include multiple Android manufacturers where possible.

Potential categories:

* Google Pixel / near-stock Android
* Samsung
* Xiaomi/Redmi
* OnePlus
* Realme
* Other major OEMs

OEM-specific battery and background-management behavior must be documented.

---

# 61. Google Play Compliance

Before release, the application must be reviewed against current Google Play policies.

Particular attention should be given to:

* Usage Access
* Accessibility-related APIs if considered
* Overlay permissions
* Background execution
* Camera usage
* Notification access
* Device/package visibility
* Fake app appearance
* Privacy disclosures
* Advertising
* Billing
* User data handling

The coding agent must not implement a technically possible mechanism if it creates a significant policy/compliance problem without first flagging it.

---

# 62. Architecture Rules for the Coding Agent

The coding agent MUST follow these rules.

### Rule 1

Use Kotlin for native Android code.

### Rule 2

Use Jetpack Compose for the application UI.

### Rule 3

Do not introduce React, React Native, Flutter, Electron, Python, or a backend unless explicitly approved.

### Rule 4

Do not introduce unnecessary dependencies.

### Rule 5

Keep Android system integrations inside the `system/` layer.

### Rule 6

Keep business logic inside the domain layer.

### Rule 7

Keep persistence behind repositories.

### Rule 8

Never store plaintext authentication credentials.

### Rule 9

Never request an optional permission before the related feature requires it.

### Rule 10

Do not implement future features while working on an earlier development phase.

### Rule 11

Do not silently change the architecture.

### Rule 12

If the planned implementation is technically impossible or unreliable, stop and explain the problem rather than creating a fake implementation.

### Rule 13

If Android behavior differs by version or OEM, document the limitation.

### Rule 14

Do not claim that a feature works universally until it has been tested.

### Rule 15

Security and reliability take priority over visual polish.

---

# 63. Architecture Change Policy

If the coding agent believes the architecture needs to change:

It must report:

```text
PROPOSED ARCHITECTURE CHANGE

Current approach:
...

Problem:
...

Proposed approach:
...

Why:
...

Advantages:
...

Disadvantages:
...

Affected modules:
...

Requires user approval:
YES
```

The agent must wait for approval before making a major architectural change.

Minor implementation details that do not alter architectural boundaries may be changed without approval.

---

# 64. Definition of Architectural Success

The architecture is considered successful when:

1. The application can be developed incrementally.
2. UI and Android system logic are separated.
3. Security-sensitive functionality has clear boundaries.
4. The app-lock engine can evolve independently of the UI.
5. Themes can be added without rewriting authentication.
6. Future privacy tools can be added without modifying the core lock engine unnecessarily.
7. Monetization can be added without making core security dependent on network services.
8. The application remains functional offline for core features.
9. The project remains understandable to a developer using an AI coding agent.
10. The architecture does not require unnecessary backend infrastructure.

---

# 65. Final Architecture

The intended architecture is:

```text
                         AppLock
                            │
             ┌──────────────┴──────────────┐
             │                             │
       Presentation                    System Layer
             │                             │
      Jetpack Compose              Android APIs
             │                             │
        ViewModels               ┌─────────┼──────────┐
             │                   │         │          │
        Use Cases             AppMonitor  Auth     Permissions
             │                   │         │          │
       Repositories              │      Biometrics   Overlay
             │                   │                    │
       ┌─────┴─────┐             │                    │
       │           │             │                    │
   DataStore      Room           │                    │
       │           │             │                    │
       └─────┬─────┘             │                    │
             │                   └────────┬───────────┘
             │                            │
             └──────────────┬─────────────┘
                            │
                     Android OS
```

---

# 66. Development Principle

The architecture must support the following development philosophy:

> **Build the smallest reliable core first. Then expand.**

The first technical milestone is not:

```text
Themes
Website blocker
Private browser
Ads
Subscriptions
Intruder detection
```

The first technical milestone is:

```text
Detect protected app
        ↓
Show lock screen
        ↓
Authenticate
        ↓
Allow access
```

If this mechanism is not reliable, additional features must not take priority.

---

# 67. Relationship With Other Documents

The project should maintain three primary planning documents:

```text
PRD.md
   ↓
What the product should do

architecture.md
   ↓
How the product should technically work

phases.md
   ↓
In what order the product should be built
```

The coding agent should consult all three before implementing a development phase.

`PRD.md` defines **product requirements**.

`architecture.md` defines **technical architecture**.

`phases.md` defines **implementation sequence and approval gates**.

No single document should replace the others.
 
---

# 68. Build Pipeline & Cloud CI/CD (GitHub Actions)

To enable development and testing on physical devices without requiring heavy local system installations (such as local Android Studio or RAM-intensive emulators), the project uses **GitHub Actions** as its primary cloud compilation engine.

### Pipeline Workflow
1. Code changes are pushed to GitHub.
2. The GitHub Actions workflow (`.github/workflows/build-apk.yml`) triggers on push or manual dispatch.
3. The workflow executes on an Ubuntu cloud runner equipped with JDK 21, the Android SDK, and Gradle.
4. The workflow runs:
   ```bash
   gradle assembleDebug --stacktrace
   ```
5. The resulting debug APK (`app-debug.apk`) is uploaded as a downloadable build artifact.
6. The developer/tester downloads the APK directly from the GitHub Actions run to their physical Android device and installs it for live validation.

### Requirements for the Coding Agent
* The agent must ensure that all code, dependencies, and resources compile cleanly with Gradle.
* The agent must maintain `gradle/libs.versions.toml`, `settings.gradle.kts`, and `app/build.gradle.kts`.
* The agent must never break headless Gradle compilation.

