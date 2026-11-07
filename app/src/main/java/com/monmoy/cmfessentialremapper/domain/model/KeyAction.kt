package com.monmoy.cmfessentialremapper.domain.model

/**
 * Supported functional actions that can be assigned to hardware key gestures.
 */
enum class KeyAction(
    val id: String,
    val title: String,
    val description: String
) {
    CYCLE_RINGER(
        id = "cycle_ringer",
        title = "Cycle Ring Mode 1",
        description = "Ring → Vibrate → Silent"
    ),
    CYCLE_RINGER_2(
        id = "cycle_ringer_2",
        title = "Cycle Ring Mode 2",
        description = "Ring → Silent → Vibrate"
    ),
    TOGGLE_FLASHLIGHT(
        id = "toggle_flashlight",
        title = "Toggle Flashlight",
        description = "Turn flashlight on or off"
    ),
    TAKE_SCREENSHOT(
        id = "take_screenshot",
        title = "Take Screenshot",
        description = "Capture the current screen"
    ),
    OPEN_NOTIFICATIONS(
        id = "open_notifications",
        title = "Toggle Notifications",
        description = "Open or close notification drawer"
    ),
    OPEN_QUICK_SETTINGS(
        id = "open_quick_settings",
        title = "Toggle Quick Settings",
        description = "Open or close quick settings panel"
    ),
    LOCK_SCREEN(
        id = "lock_screen",
        title = "Lock Screen",
        description = "Turn off and lock the display"
    ),
    NONE(
        id = "none",
        title = "Do Nothing",
        description = "No action assigned"
    );

    companion object {
        fun fromId(id: String?): KeyAction {
            return entries.firstOrNull { it.id == id } ?: NONE
        }
    }
}
