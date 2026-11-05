package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRingerModeRepository(
    initialMode: RingerMode = RingerMode.RING,
    var shouldFailSetMode: Boolean = false,
    var hasNotificationPolicyAccessGranted: Boolean = true
) : RingerModeRepository {

    private val _modeFlow = MutableStateFlow(initialMode)
    private val _learnedKeyFlow = MutableStateFlow(Pair(0, 250))
    private val _isConfiguredFlow = MutableStateFlow(false)

    var setModeCallCount: Int = 0
        private set

    override fun observeRingerMode(): Flow<RingerMode> = _modeFlow.asStateFlow()

    override fun getCurrentRingerMode(): RingerMode = _modeFlow.value

    override fun hasNotificationPolicyAccess(): Boolean = hasNotificationPolicyAccessGranted

    override suspend fun setRingerMode(mode: RingerMode) {
        if (shouldFailSetMode) {
            throw IllegalStateException("Hardware audio service error")
        }
        setModeCallCount++
        _modeFlow.value = mode
    }

    override fun observeEssentialKeyStatus(): Flow<EssentialKeyStatus> {
        return MutableStateFlow(EssentialKeyStatus.NotConfigured)
    }

    override fun observeLearnedKey(): Flow<Pair<Int, Int>> = _learnedKeyFlow.asStateFlow()

    override fun observeIsKeyConfigured(): Flow<Boolean> = _isConfiguredFlow.asStateFlow()

    override suspend fun saveLearnedKey(keyCode: Int, scanCode: Int) {
        _learnedKeyFlow.value = Pair(keyCode, scanCode)
        _isConfiguredFlow.value = true
    }

    override suspend fun resetLearnedKey() {
        _learnedKeyFlow.value = Pair(0, 250)
        _isConfiguredFlow.value = false
    }
}
