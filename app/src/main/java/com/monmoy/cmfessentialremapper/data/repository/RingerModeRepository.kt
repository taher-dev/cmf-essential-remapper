package com.monmoy.cmfessentialremapper.data.repository

import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface providing ringer mode, key configuration, and system status data.
 */
interface RingerModeRepository {
    /**
     * Observes live changes to the device's ringer mode via system broadcasts.
     */
    fun observeRingerMode(): Flow<RingerMode>

    /**
     * Retrieves the current device ringer mode.
     */
    fun getCurrentRingerMode(): RingerMode

    /**
     * Sets the device ringer mode to the requested [mode].
     */
    suspend fun setRingerMode(mode: RingerMode)

    /**
     * Checks if Notification Policy Access (required by Android for Silent mode) is granted.
     */
    fun hasNotificationPolicyAccess(): Boolean

    /**
     * Checks if Do Not Disturb (DND) / notification interruption filter is currently active.
     */
    fun isDoNotDisturbActive(): Boolean = false

    /**
     * Disables Do Not Disturb and restores normal interruption filtering.
     */
    suspend fun disableDoNotDisturb() {}

    /**
     * Observes current Essential Key configuration status.
     */
    fun observeEssentialKeyStatus(): Flow<EssentialKeyStatus>

    /**
     * Observes learned key code and scan code.
     */
    fun observeLearnedKey(): Flow<Pair<Int, Int>>

    /**
     * Observes whether the key has been configured.
     */
    fun observeIsKeyConfigured(): Flow<Boolean>

    /**
     * Persists the learned key code and scan code.
     */
    suspend fun saveLearnedKey(keyCode: Int, scanCode: Int)

    /**
     * Resets the learned key configuration.
     */
    suspend fun resetLearnedKey()
}
