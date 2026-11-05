package com.monmoy.cmfessentialremapper.utils

import android.media.AudioManager
import com.monmoy.cmfessentialremapper.domain.model.RingerMode

object AudioUtils {
    /**
     * Converts Android framework [AudioManager.RINGER_MODE_*] integers into domain [RingerMode].
     */
    fun intToRingerMode(mode: Int): RingerMode {
        return when (mode) {
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.RING
            else -> RingerMode.RING
        }
    }

    /**
     * Converts domain [RingerMode] into Android framework [AudioManager.RINGER_MODE_*] integer.
     */
    fun ringerModeToInt(mode: RingerMode): Int {
        return when (mode) {
            RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
            RingerMode.RING -> AudioManager.RINGER_MODE_NORMAL
        }
    }
}

