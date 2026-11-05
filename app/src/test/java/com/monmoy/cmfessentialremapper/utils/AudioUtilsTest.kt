package com.monmoy.cmfessentialremapper.utils

import android.media.AudioManager
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioUtilsTest {

    @Test
    fun testRingerModeConversion() {
        assertEquals(RingerMode.SILENT, AudioUtils.intToRingerMode(AudioManager.RINGER_MODE_SILENT))
        assertEquals(RingerMode.VIBRATE, AudioUtils.intToRingerMode(AudioManager.RINGER_MODE_VIBRATE))
        assertEquals(RingerMode.RING, AudioUtils.intToRingerMode(AudioManager.RINGER_MODE_NORMAL))
        assertEquals(RingerMode.RING, AudioUtils.intToRingerMode(999))
    }

    @Test
    fun testRingerModeToIntConversion() {
        assertEquals(AudioManager.RINGER_MODE_SILENT, AudioUtils.ringerModeToInt(RingerMode.SILENT))
        assertEquals(AudioManager.RINGER_MODE_VIBRATE, AudioUtils.ringerModeToInt(RingerMode.VIBRATE))
        assertEquals(AudioManager.RINGER_MODE_NORMAL, AudioUtils.ringerModeToInt(RingerMode.RING))
    }
}

