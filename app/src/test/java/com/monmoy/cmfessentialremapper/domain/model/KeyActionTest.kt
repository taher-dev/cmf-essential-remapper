package com.monmoy.cmfessentialremapper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyActionTest {

    @Test
    fun testFromIdResolvesCorrectKeyAction() {
        assertEquals(KeyAction.CYCLE_RINGER, KeyAction.fromId("cycle_ringer"))
        assertEquals(KeyAction.CYCLE_RINGER_2, KeyAction.fromId("cycle_ringer_2"))
        assertEquals(KeyAction.TOGGLE_FLASHLIGHT, KeyAction.fromId("toggle_flashlight"))
        assertEquals(KeyAction.TAKE_SCREENSHOT, KeyAction.fromId("take_screenshot"))
        assertEquals(KeyAction.OPEN_NOTIFICATIONS, KeyAction.fromId("open_notifications"))
        assertEquals(KeyAction.OPEN_QUICK_SETTINGS, KeyAction.fromId("open_quick_settings"))
        assertEquals(KeyAction.LOCK_SCREEN, KeyAction.fromId("lock_screen"))
        assertEquals(KeyAction.NONE, KeyAction.fromId("none"))
    }

    @Test
    fun testFromIdFallsBackToNoneOnUnknownOrRemovedId() {
        assertEquals(KeyAction.NONE, KeyAction.fromId("set_silent"))
        assertEquals(KeyAction.NONE, KeyAction.fromId("set_vibrate"))
        assertEquals(KeyAction.NONE, KeyAction.fromId("set_ring"))
        assertEquals(KeyAction.NONE, KeyAction.fromId("invalid_unknown_action"))
        assertEquals(KeyAction.NONE, KeyAction.fromId(null))
    }
}
