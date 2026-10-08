package com.applock.privacy.feature.disguise

enum class DisguiseMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String
) {
    NONE(
        id = "NONE",
        title = "No Disguise (Default)",
        subtitle = "Direct lock screen",
        description = "Immediately presents the secure PIN or Pattern lock screen when protected apps open."
    ),
    CRASH_DIALOG(
        id = "CRASH_DIALOG",
        title = "Fake Crash Dialog",
        subtitle = "Displays 'App has stopped'",
        description = "Mimics an Android system crash dialog. Prying eyes think the app is broken. Long-press 'Close app' or triple-tap to reveal the real lock screen."
    ),
    CALCULATOR(
        id = "CALCULATOR",
        title = "Calculator Decoy",
        subtitle = "Functional math calculator",
        description = "Replaces the lock screen with a working calculator. Enter your secret PIN and tap '=' to seamlessly unlock."
    );

    companion object {
        fun fromId(id: String?): DisguiseMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}
