# AppLock — Development Phases

**Project:** AppLock
**Platform:** Android
**Primary Requirements:** `PRD.md`
**Technical Architecture:** `architecture.md`
**Purpose:** Define the exact development sequence and prevent the coding agent from implementing multiple phases at once.

---

# 1. Critical Agent Rule

This document is a **development control system**.

The coding agent MUST work on **one phase at a time**.

### The agent must NEVER:

* Implement multiple phases in one task.
* Automatically continue to the next phase.
* Implement future features "while already in the code."
* Add unrequested functionality.
* Assume that completing a phase means the next phase is approved.
* Rewrite the architecture without approval.
* Add monetization before its designated phase.
* Implement future features merely because the required code structure already exists.

### Required behavior

If the user says:

> "Start Phase 1."

The agent must implement **Phase 1 only**.

When Phase 1 is complete, the agent must stop and report:

```text
PHASE 1 COMPLETE
STATUS: AWAITING APPROVAL
```

The agent must wait for explicit user approval.

---

# 2. Approval System

Every phase follows:

```text
NOT STARTED
      ↓
IN PROGRESS
      ↓
TESTING
      ↓
AWAITING APPROVAL
      ↓
APPROVED
```

A phase becomes **APPROVED** only when the user explicitly confirms it.

Examples:

```text
"Phase 1 approved."

"Phase 1 is correct."

"Looks good, move to Phase 2."

"Start Phase 2."
```

The agent must NOT interpret these as approval:

* "Okay"
* "Nice"
* "Thanks"
* Asking another question
* Testing part of the feature
* Silence
* General positive feedback

If approval is ambiguous, ask the user.

---

# 3. Phase Completion Report

At the end of every phase, the agent must provide:

## Implemented

List what was actually built.

## Files Changed

List created/modified files.

## Technical Decisions

Mention important implementation decisions.

## Testing

Explain exactly how the user can test the phase.

## Known Issues

List unresolved issues or limitations.

## Phase Status

Use exactly:

```text
PHASE STATUS: AWAITING APPROVAL
```

Then STOP.

---

# 4. Phase 0 — Technical Validation

## Objective

Validate the technical feasibility of the AppLock core before significant development begins.

## Tasks

* Read `PRD.md`.
* Read `architecture.md`.
* Validate the proposed Android architecture.
* Determine appropriate Android SDK versions.
* Research the required Android APIs.
* Validate the proposed application-monitoring approach.
* Validate the proposed lock-screen presentation mechanism.
* Identify Android version limitations.
* Identify OEM-specific risks.
* Identify Google Play policy concerns.
* Confirm the authentication architecture.
* Confirm local-storage architecture.
* Confirm required permissions.

## Deliverable

Produce a technical validation report containing:

```text
Android version strategy
Required APIs
App monitoring strategy
Lock screen strategy
Permission strategy
Authentication strategy
Storage strategy
Security concerns
OEM concerns
Play Store concerns
Known limitations
```

## Restrictions

Do NOT build the complete application.

Do NOT implement:

* Full app locker
* Themes
* Website blocker
* Private browser
* Ads
* Billing
* Intruder detection

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 5. Phase 1 — Project Foundation

## Objective

Create a clean, buildable native Android project.

## Technology

Use the approved architecture:

* Kotlin
* Jetpack Compose
* AndroidX
* Gradle
* MVVM/layered architecture

## Tasks

* Create Android project.
* Configure application ID.
* Configure Gradle.
* Configure Kotlin.
* Configure Compose.
* Configure minimum/target SDK according to Phase 0.
* Create package structure.
* Create application class.
* Create main activity.
* Create navigation foundation.
* Create basic theme system.
* Create reusable UI foundation.
* Create basic local storage foundation.
* Configure GitHub Actions cloud build workflow (`.github/workflows/build-apk.yml`) for headless APK generation.

## Initial navigation

```text
Home
Tools
Themes
Settings
```

The screens may initially contain placeholder content.

## Do NOT implement

* App discovery
* Usage Access
* Overlay permission
* PIN
* Pattern
* Biometrics
* App locking
* Website blocking
* Private browser
* Notifications
* Intruder detection
* Ads
* Billing
* Subscription

## Acceptance Criteria

* Project builds cleanly via Gradle / GitHub Actions.
* GitHub Actions workflow succeeds and produces `app-debug.apk`.
* App installs.
* App launches.
* Navigation between Home, Tools, Themes, and Settings works.
* No major crashes.
* Compose theme works.
* Project structure matches `architecture.md`.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 6. Phase 2 — Design System & App Shell

## Objective

Create the reusable visual foundation and primary application shell.

## Tasks

Build:

* App typography
* Colors
* Spacing system
* Shapes
* Buttons
* Cards
* Toggles
* Top bars
* Bottom navigation
* Empty states
* Loading states
* Error states

Build initial versions of:

```text
Home
Tools
Themes
Settings
```

## Important

These screens should establish the final visual language but do not need complete functionality.

## Do NOT implement

* App-lock engine
* Authentication
* Permission logic
* Monetization
* Future tools

## Acceptance Criteria

The application looks and behaves like a coherent product rather than a collection of default Compose screens.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 7. Phase 3 — First-Launch Onboarding

## Objective

Build the complete first-launch onboarding UI and state management.

## Flow

```text
Splash
  ↓
Why are you using AppLock?
  ↓
Skip / Continue
  ↓
Onboarding completion
  ↓
Home
```

## Tasks

* Splash screen
* Why AppLock screen
* Reason selection
* Skip
* Continue
* Onboarding state persistence
* Completion screen
* First-launch detection

## Do NOT implement

Actual Android permission functionality.

Do not implement:

* Usage Access
* Overlay
* Battery optimization
* Authentication
* App locking

## Acceptance Criteria

* Onboarding appears on first launch.
* Optional question can be skipped.
* Completion is persisted.
* Relaunch does not unnecessarily repeat onboarding.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 8. Phase 4 — Permission System

## Objective

Implement the Android permissions required by the core AppLock functionality.

## Implement

### Usage Access

* Explanation screen
* Settings redirect
* Permission detection
* Return handling

### Display Over Other Apps

* Explanation
* Settings redirect
* Permission detection

### Battery Optimization

* Explanation
* Appropriate settings flow
* State detection where supported

## Requirements

The UI must clearly show:

```text
Enabled
Disabled
```

for each capability.

## Important

Never assume a permission was granted merely because the user returned from Android settings.

Always re-check the actual state.

## Do NOT implement

* App-lock interception
* PIN
* Pattern
* Biometrics
* Intruder detection

## Acceptance Criteria

The user can:

1. Open the relevant Android settings.
2. Grant or deny the permission.
3. Return to AppLock.
4. See the correct permission state.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 9. Phase 5 — Installed App Discovery

## Objective

Discover applications installed on the device.

## Tasks

* Integrate PackageManager.
* Retrieve supported applications.
* Create AppInfo model.
* Display app icon.
* Display app name.
* Display package identifier where needed.
* Distinguish user apps from system apps.
* Add search.
* Handle missing/uninstalled applications.

## UI

```text
Choose Apps

Search...

Instagram
WhatsApp
YouTube
Chrome
```

## Do NOT implement

Actual app locking.

The user can select apps, but selecting an app does not yet intercept its launch.

## Acceptance Criteria

* Installed applications appear correctly.
* Search works.
* App icons load.
* Uninstalled apps are handled.
* App information is stable.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 10. Phase 6 — Protected App Management

## Objective

Allow users to mark applications as protected.

## Tasks

* Protected-app model
* Repository
* Persistence
* Lock/unlock state
* Home screen locked/unlocked sections
* Lock/unlock interaction
* Search/filtering
* State restoration

## Home behavior

```text
[ Locked ] [ Unlocked ]

Instagram        🔒
WhatsApp         🔒
YouTube          🔓
Chrome           🔓
```

Tapping the state control moves the application between states.

## Do NOT implement

The actual lock interception mechanism.

## Acceptance Criteria

* User can protect an app.
* User can unprotect an app.
* State persists after restart.
* Home screen accurately reflects state.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 11. Phase 7 — Authentication Foundation

## Objective

Implement the complete authentication system independently of app interception.

## PIN

Implement:

* 4-digit PIN
* 6-digit PIN
* PIN creation
* PIN confirmation
* PIN validation
* Change PIN
* Secure storage

## Pattern

Implement:

* Pattern creation
* Pattern confirmation
* Pattern validation
* Change pattern
* Secure storage

## Biometrics

Implement:

* Availability detection
* Enable/disable
* Android BiometricPrompt
* Authentication result handling
* Fallback authentication

## Security

Never store plaintext credentials.

Use the approved secure-storage approach from `architecture.md`.

## Do NOT implement

The app-monitoring/locking engine.

## Acceptance Criteria

The user can successfully create and authenticate using the configured method.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 12. Phase 8 — Core App Monitoring

## Objective

Detect when a protected application becomes active.

This phase is intentionally separate from the lock-screen implementation.

## Tasks

* Implement app-monitoring service/system integration.
* Detect foreground application changes.
* Compare active package against protected apps.
* Handle Usage Access state.
* Minimize unnecessary polling.
* Handle app switching.
* Handle lifecycle changes.

## Required test

The system should correctly determine:

```text
Instagram opened
      ↓
Instagram is protected
      ↓
Authentication is required
```

But this phase does not need to display the final lock screen yet.

## Do NOT implement

* Final lock screen
* Themes
* Intruder detection
* Alarm

## Acceptance Criteria

The monitoring system reliably identifies protected-app launches on the supported test device(s).

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 13. Phase 9 — Core Lock Engine

## Objective

Connect app monitoring to authentication and create the actual AppLock experience.

This is the most important phase.

## Flow

```text
Protected app opens
        ↓
Monitor detects package
        ↓
Check protected state
        ↓
Check authentication state
        ↓
Authentication required
        ↓
Show lock screen
        ↓
User authenticates
        ↓
Success
        ↓
Allow access
```

## Tasks

* Lock state manager
* Authentication session
* Lock-screen launch
* Authentication result handling
* App access flow
* Incorrect authentication handling
* App switching
* Recent apps handling
* Screen off/on
* Reopening protected apps
* Prevent authentication loops
* Handle AppLock itself safely

## Critical Requirement

This phase must prioritize **reliability over UI polish**.

## Testing

Test:

* Open protected app.
* Enter correct PIN.
* Enter incorrect PIN.
* Switch to another app.
* Return to protected app.
* Close and reopen protected app.
* Lock/unlock phone.
* Restart phone.
* Revoke permissions.
* Protect multiple apps.

## Do NOT implement

* Premium themes
* Ads
* Billing
* Website blocker
* Private browser
* Intruder detection

## Acceptance Criteria

The fundamental product works:

```text
Select app
 ↓
Lock app
 ↓
Open app
 ↓
AppLock appears
 ↓
Authenticate
 ↓
App opens
```

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 14. Phase 10 — Lock Screen Polish

## Objective

Turn the functional lock screen into the primary polished AppLock experience.

## Tasks

* Final PIN keypad
* Pattern UI
* Biometric button
* App icon/name
* Error states
* Loading states
* Animations
* Haptic feedback
* Dark mode
* Accessibility improvements
* Responsive layouts

## Acceptance Criteria

* Lock screen is fast.
* Authentication remains reliable.
* UI works on different screen sizes.
* Accessibility labels exist where appropriate.
* Animations never interfere with authentication.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 15. Phase 11 — Standard Themes

## Objective

Implement the free theme system.

## Tasks

* Theme model
* Theme repository
* Theme selector
* Standard themes
* Custom colors
* PIN background color
* PIN text color
* Pattern color
* Accent color
* Theme persistence
* Apply theme

## Free functionality

All basic color customization is free.

## Acceptance Criteria

Changing a theme immediately updates the lock screen and persists after restart.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 16. Phase 12 — My Themes & Custom Themes

## Objective

Allow users to create their own lock-screen themes.

## Tasks

* Custom theme editor
* Image picker
* Background image
* PIN background
* PIN text
* Pattern color
* Preview
* Save
* Delete
* My Themes
* Apply custom theme

## Initial monetization behavior

Custom theme creation may later require a rewarded advertisement.

However, if monetization is not yet implemented, build the theme system without fake ads.

## Acceptance Criteria

Users can create, save, view, apply, and delete custom themes.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 17. Phase 13 — Theme Monetization

## Objective

Implement monetization for eligible themes.

## Rewarded Ads

Users may unlock selected themes by completing a rewarded advertisement.

Flow:

```text
Select locked theme
       ↓
Watch rewarded ad
       ↓
Ad completed
       ↓
Theme unlocked
       ↓
Theme applied
```

If the ad is skipped, interrupted, or fails to complete:

```text
Theme remains locked
```

## Theme Purchase

Potential one-time purchase:

```text
₹9
```

The actual price must be configurable.

## Requirements

* Purchase entitlement
* Restore purchases
* Rewarded-ad entitlement
* My Themes integration
* Failure handling

## Do NOT implement

Subscription yet.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 18. Phase 14 — Settings & Core Customization

## Objective

Complete the main Settings experience.

## Security

* Change PIN
* Change pattern
* Biometrics
* Authentication settings

## Lock Screen

* Theme
* Animation
* Vibration
* Pattern visibility

## Appearance

* Dark mode
* Accent colors

## Permission Status

Display:

* Usage Access
* Overlay
* Battery optimization status

## Acceptance Criteria

Every visible setting must perform a real action.

There must be no fake switches.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 19. Phase 15 — Failed Attempt Protection

## Objective

Implement basic protection against repeated incorrect authentication.

## Tasks

* Failed-attempt counter
* Configurable thresholds
* Temporary lockout
* Remaining-time display
* Safe reset rules

Initial thresholds:

```text
3
5
10
```

Initial lockout strategy may be:

```text
1 minute
5 minutes
10 minutes
```

The exact algorithm should be finalized during implementation.

## Critical Requirement

Never permanently lock the legitimate user out of their device or protected application.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 20. Phase 16 — Intruder Detection

## Objective

Capture evidence after repeated failed authentication attempts when explicitly enabled.

## Tasks

* Feature settings
* Camera permission
* Threshold configuration
* Camera capture
* Local storage
* Security event
* Event timestamp
* Security history

## Privacy Requirements

* Explain camera usage.
* Request permission only when the feature is enabled.
* Keep images local by default.
* Do not upload images without explicit consent.

## Acceptance Criteria

Configured threshold:

```text
3 / 5 / 10 failed attempts
```

causes the configured intruder action.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 21. Phase 17 — Intruder Alarm

## Objective

Add an optional alarm after repeated failed attempts.

## Tasks

* Alarm enable/disable
* Threshold
* Sound selection
* Duration
* Stop mechanism

## Critical Requirement

A legitimate user must have a reliable way to stop the alarm after successful authentication.

Never create an unrecoverable state.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 22. Phase 18 — Notification Security

## Objective

Add privacy protection for notifications from protected applications.

## Tasks

* Research current Android notification APIs.
* Implement permission flow if required.
* Identify notifications from protected apps.
* Hide or minimize sensitive notification content where technically supported.
* Add settings.

Example:

```text
Before:

WhatsApp
Rahul: Where are you?

After:

WhatsApp
New notification
```

## Important

Behavior may differ between Android versions.

Do not promise functionality that the platform cannot guarantee.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 23. Phase 19 — Website Blocker

## Objective

Add website blocking as an independent privacy tool.

## Tasks

* Website list
* Add website
* Remove website
* Enable/disable
* Blocking engine
* Settings
* User feedback when a site is blocked

## Important

Before implementation, validate the technical mechanism and Android/browser limitations.

Do not claim universal blocking across all browsers unless verified.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 24. Phase 20 — Private Browser

## Objective

Add an in-app private browsing experience.

## Tasks

* Browser UI
* Address/search bar
* Navigation
* Session management
* Back/forward
* Refresh
* Session clearing
* Exit behavior

## Privacy Requirement

The feature should clearly state that it clears local session data according to its implementation.

Do not claim:

* Complete anonymity
* Invisible browsing
* Protection from network providers
* Protection from websites

unless technically verified.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 25. Phase 21 — Fake App Appearance

## Objective

Explore and potentially implement alternate launcher appearance.

Potential options:

```text
Calculator
Weather
Clock
Music
Notes
```

## Mandatory prerequisite

Before implementation, verify:

* Android launcher behavior
* Package/application identity limitations
* Google Play policies
* Security implications
* User experience

If the intended implementation is not compliant or reliable, STOP and report the limitation.

Do not implement a deceptive mechanism that violates platform policies.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 26. Phase 22 — Pro Subscription

## Objective

Implement the premium subscription system after core functionality is stable.

Potential Pro features:

* Intruder detection
* Advanced customization
* Advanced security controls
* Premium lock-screen features
* Premium themes
* Other finalized Pro functionality

## Tasks

* Define subscription product
* Google Play Billing integration
* Purchase flow
* Restore purchases
* Entitlement state
* Expiration handling
* Graceful billing failure
* Pro UI

## Critical Rule

Core AppLock functionality must remain usable without an active subscription.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 27. Phase 23 — Advanced UI Polish

## Objective

Polish the complete application.

## Tasks

* Animations
* Transitions
* Empty states
* Error states
* Loading states
* Accessibility
* Responsive layouts
* Dark mode refinement
* Typography refinement
* Icon consistency
* Haptic feedback refinement

## Important

Do not sacrifice authentication speed or security for animation.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 28. Phase 24 — Performance & Battery Optimization

## Objective

Reduce resource consumption without damaging locking reliability.

## Test:

* CPU usage
* Memory
* Battery consumption
* Background behavior
* App launch latency
* Lock-screen latency

## Optimize:

* App monitoring
* Database operations
* Compose recomposition
* Background work
* Image loading
* Theme resources

## Critical Rule

Never optimize by weakening the lock mechanism without explicit approval.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 29. Phase 25 — Security Audit

## Objective

Review the complete application from a security perspective.

## Audit:

### Authentication

* PIN storage
* Pattern storage
* Biometric fallback
* Authentication sessions

### App Locking

* Bypass scenarios
* Recent apps
* App switching
* Screen lock/unlock
* Device restart

### Data

* Sensitive local data
* Logs
* Images
* Security events

### Permissions

* Requested permissions
* Unnecessary permissions
* Permission revocation

### Network

* Unnecessary network access
* Sensitive data transmission

## Completion

Produce a security audit report.

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 30. Phase 26 — Compatibility Testing

## Objective

Test AppLock across Android versions and OEM environments.

## Test categories

### Android versions

Test supported Android versions defined by the project.

### OEMs

Where devices are available, test:

* Google Pixel
* Samsung
* Xiaomi/Redmi
* OnePlus
* Realme
* Other major OEMs

## Test:

* App monitoring
* Lock screen
* Background behavior
* Battery optimization
* Permissions
* Biometrics
* Device restart
* App switching
* Recent apps

## Output

Create:

```text
COMPATIBILITY_REPORT.md
```

containing:

```text
Device
Android version
Result
Known issues
Workaround
Severity
```

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 31. Phase 27 — Google Play Preparation

## Objective

Prepare the application for production distribution.

## Tasks

* Review current Google Play policies.
* Review permission declarations.
* Review privacy requirements.
* Prepare privacy policy requirements.
* Prepare Data Safety information.
* Prepare store listing.
* Prepare screenshots.
* Prepare app icon.
* Prepare release configuration.
* Prepare signing configuration.
* Build release AAB.

## Important

Do not publish until the user explicitly approves publication.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 32. Phase 28 — Release Candidate

## Objective

Create a production-ready release candidate.

## Tasks

* Build release AAB.
* Run final regression testing.
* Verify authentication.
* Verify app locking.
* Verify permissions.
* Verify themes.
* Verify monetization.
* Verify premium features.
* Verify privacy behavior.
* Verify crash-free startup.
* Verify app restoration/relaunch.

## Acceptance Criteria

No known critical security or functionality issues remain.

## Completion

```text
PHASE STATUS: AWAITING APPROVAL
```

STOP.

---

# 33. Phase 29 — Post-Launch Improvements

This phase begins only after the application has been released.

Potential work:

* Bug fixes
* Performance improvements
* Compatibility fixes
* User-requested features
* Additional themes
* Additional privacy tools
* New monetization experiments

Each post-launch feature should become its own approved task or mini-phase.

Do not turn this into an uncontrolled feature backlog.

---

# 34. Global Rules

These rules apply to every phase.

## Rule 1 — One Phase Only

Never implement more than the requested phase.

## Rule 2 — Read the Documents

Before starting a phase, read:

```text
PRD.md
architecture.md
phases.md
```

and determine what applies to the current phase.

## Rule 3 — Do Not Guess

If an important requirement is ambiguous:

Ask before implementing.

## Rule 4 — Do Not Hide Problems

If something cannot be implemented as specified:

Explain the problem.

Do not create fake functionality.

## Rule 5 — No Silent Architecture Changes

Major architectural changes require approval.

## Rule 6 — Test Before Completion

A phase is not complete merely because the code compiles.

The implementation must satisfy that phase's acceptance criteria.

## Rule 7 — No Future Features

Do not implement features belonging to later phases.

## Rule 8 — No Premature Monetization

Do not add ads, billing, or subscriptions before their designated phases.

## Rule 9 — Security First

If security conflicts with convenience:

Prioritize security.

## Rule 10 — Reliability First

If visual polish conflicts with AppLock reliability:

Prioritize AppLock reliability.

---

# 35. Agent Response Format

When beginning a phase, respond with:

```text
PHASE X — [NAME]

Goal:
[Short explanation]

Scope:
[What will be implemented]

Out of scope:
[What will NOT be implemented]

Files likely to change:
[List]

Implementation plan:
[Short plan]
```

Then implement only that phase.

---

# 36. Agent Completion Format

When the phase is finished:

```text
PHASE X COMPLETE

Implemented:
- ...
- ...
- ...

Files changed:
- ...
- ...

Testing:
1. ...
2. ...
3. ...

Known issues:
- ...

PHASE STATUS: AWAITING APPROVAL
```

Then STOP.

---

# 37. Phase Approval Command

The user can explicitly authorize the next phase using:

```text
Phase X approved. Start Phase X+1.
```

The agent must then begin only the requested phase.

---

# 38. Emergency Stop Rule

At any point, the user may say:

```text
STOP
```

or:

```text
Stop here.
```

The agent must immediately stop implementing and report the current state.

---

# 39. Final Development Sequence

The complete high-level sequence is:

```text
Phase 0
Technical Validation
       ↓
Phase 1
Project Foundation
       ↓
Phase 2
Design System & App Shell
       ↓
Phase 3
Onboarding
       ↓
Phase 4
Permissions
       ↓
Phase 5
App Discovery
       ↓
Phase 6
Protected App Management
       ↓
Phase 7
Authentication
       ↓
Phase 8
App Monitoring
       ↓
Phase 9
Core Lock Engine
       ↓
Phase 10
Lock Screen Polish
       ↓
Phase 11
Standard Themes
       ↓
Phase 12
Custom Themes
       ↓
Phase 13
Theme Monetization
       ↓
Phase 14
Settings
       ↓
Phase 15
Failed Attempt Protection
       ↓
Phase 16
Intruder Detection
       ↓
Phase 17
Intruder Alarm
       ↓
Phase 18
Notification Security
       ↓
Phase 19
Website Blocker
       ↓
Phase 20
Private Browser
       ↓
Phase 21
Fake App Appearance
       ↓
Phase 22
Pro Subscription
       ↓
Phase 23
UI Polish
       ↓
Phase 24
Performance
       ↓
Phase 25
Security Audit
       ↓
Phase 26
Compatibility Testing
       ↓
Phase 27
Play Store Preparation
       ↓
Phase 28
Release Candidate
       ↓
Phase 29
Post-Launch
```

---

# 40. Final Rule

> **The coding agent must never decide what to build next. The user decides what phase to start.**

The agent's responsibility is to implement the requested phase correctly, test it, report its status, and wait for approval.

```text
USER
  ↓
SELECTS PHASE
  ↓
AI IMPLEMENTS PHASE
  ↓
AI TESTS PHASE
  ↓
AI REPORTS
  ↓
AWAITING APPROVAL
  ↓
USER TESTS
  ↓
USER APPROVES
  ↓
NEXT PHASE
```

This approval loop is mandatory throughout the entire AppLock development process.
