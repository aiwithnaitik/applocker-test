# Rork Development Rules — AppLock

**Project:** AppLock
**Platform:** Native Android (Kotlin + Jetpack Compose)
**AI Agent:** Rork / Coding Agent
**Primary Documents:**
* `PRD.md` — Product Requirements ("What to build")
* `architecture.md` — Technical Architecture ("How to build it")
* `phases.md` — Development Sequence ("When to build it")

---

# 1. Core Operating Principles

1. **Native Android Stack Only**: AppLock is a native Android system utility. It is implemented in **Kotlin + Jetpack Compose**. Do not attempt to rewrite or convert this project to React, React Native, or web frameworks, as system window overlays and background app monitoring cannot function in sandboxed cross-platform web shells.
2. **Phase-by-Phase Discipline**: The agent must follow `phases.md` strictly. Work on **one phase at a time**. Never implement future phases or features without explicit user authorization.
3. **No Fake / Placeholder Functionality**: Do not simulate system features with dummy hardcoded lists. Real system permissions and native Android APIs must be used.
4. **Cloud Build Pipeline (GitHub Actions)**: The local environment is lightweight and does not run local Android Studio or emulators. All builds and debug APKs are generated via GitHub Actions (`.github/workflows/build-apk.yml`).

---

# 2. Build & Verification Process

* Whenever changes are made, verify that Gradle configuration files (`gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`) are valid and error-free.
* Changes are pushed to GitHub, triggering `.github/workflows/build-apk.yml`.
* The resulting `app-debug.apk` is downloaded by the user to their physical Android phone for live hardware testing.
* When completing a phase, provide a completion report and wait for user approval before proceeding.
