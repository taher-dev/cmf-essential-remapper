package com.monmoy.cmfessentialremapper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RingerModeTest {

    @Test
    fun testCyclingSilentToVibrate() {
        val current = RingerMode.SILENT
        val next = current.next()
        assertEquals(RingerMode.VIBRATE, next)
    }

    @Test
    fun testCyclingVibrateToRing() {
        val current = RingerMode.VIBRATE
        val next = current.next()
        assertEquals(RingerMode.RING, next)
    }

    @Test
    fun testCyclingRingToSilent() {
        val current = RingerMode.RING
        val next = current.next()
        assertEquals(RingerMode.SILENT, next)
    }

    @Test
    fun testFromAudioManagerModeMapping() {
        assertEquals(RingerMode.SILENT, RingerMode.fromAudioManagerMode(0))
        assertEquals(RingerMode.VIBRATE, RingerMode.fromAudioManagerMode(1))
        assertEquals(RingerMode.RING, RingerMode.fromAudioManagerMode(2))
    }

    @Test
    fun testInvalidOrUnsupportedSystemModeMappingDefaultsToRing() {
        assertEquals(RingerMode.RING, RingerMode.fromAudioManagerMode(-1))
        assertEquals(RingerMode.RING, RingerMode.fromAudioManagerMode(999))
        assertEquals(RingerMode.RING, RingerMode.fromAudioManagerMode(Int.MAX_VALUE))
    }
}
