package com.monmoy.cmfessentialremapper.domain.model

/**
 * Represents the device audio ring mode.
 * Completely independent of Android framework classes.
 */
enum class RingerMode(val displayName: String) {
    SILENT("Silent"),
    VIBRATE("Vibrate"),
    RING("Ring");

    /**
     * Cycles to the next mode in sequence: SILENT -> VIBRATE -> RING -> SILENT.
     */
    fun next(): RingerMode {
        return when (this) {
            SILENT -> VIBRATE
            VIBRATE -> RING
            RING -> SILENT
        }
    }

    companion object {
        fun fromAudioManagerMode(mode: Int): RingerMode {
            return when (mode) {
                0 -> SILENT   // AudioManager.RINGER_MODE_SILENT
                1 -> VIBRATE  // AudioManager.RINGER_MODE_VIBRATE
                2 -> RING     // AudioManager.RINGER_MODE_NORMAL
                else -> RING
            }
        }
    }
}
