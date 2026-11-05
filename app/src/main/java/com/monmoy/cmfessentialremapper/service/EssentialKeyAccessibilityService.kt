package com.monmoy.cmfessentialremapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepositoryImpl
import com.monmoy.cmfessentialremapper.domain.usecase.CycleRingerModeUseCase
import com.monmoy.cmfessentialremapper.utils.KeyGesture
import com.monmoy.cmfessentialremapper.utils.KeyGestureClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RawKeyEventDetails(
    val keyCode: Int,
    val scanCode: Int,
    val action: Int,
    val repeatCount: Int,
    val deviceId: Int,
    val eventTime: Long
)

/**
 * Android Accessibility Service specifically configured to detect and filter physical
 * Essential Key events on CMF Phone 2 Pro without accessing screen content or text fields.
 */
class EssentialKeyAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var cycleRingerModeUseCase: CycleRingerModeUseCase
    private var gestureClassifier: KeyGestureClassifier? = null

    private var targetKeyCode: Int = 0
    private var targetScanCode: Int = 250
    private var isHapticFeedbackEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(applicationContext)
        val repository = RingerModeRepositoryImpl(applicationContext, dataStoreManager)
        cycleRingerModeUseCase = CycleRingerModeUseCase(repository)

        gestureClassifier = KeyGestureClassifier(
            scope = serviceScope,
            onGestureRecognized = ::handleGestureRecognized
        )

        observeSettings()
    }

    private fun observeSettings() {
        serviceScope.launch {
            launch {
                dataStoreManager.learnedKeyCode.collect { code ->
                    targetKeyCode = code
                    _savedKeyCodeFlow.value = code
                }
            }
            launch {
                dataStoreManager.learnedScanCode.collect { scan ->
                    targetScanCode = scan
                    _savedScanCodeFlow.value = scan
                }
            }
            launch {
                dataStoreManager.doublePressWindowMs.collect { window ->
                    gestureClassifier?.doublePressWindowMs = window
                }
            }
            launch {
                dataStoreManager.triplePressWindowMs.collect { window ->
                    gestureClassifier?.triplePressWindowMs = window
                }
            }
            launch {
                dataStoreManager.longPressThresholdMs.collect { threshold ->
                    gestureClassifier?.longPressThresholdMs = threshold
                }
            }
            launch {
                dataStoreManager.isHapticFeedbackEnabled.collect { enabled ->
                    isHapticFeedbackEnabled = enabled
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val details = RawKeyEventDetails(
            keyCode = event.keyCode,
            scanCode = event.scanCode,
            action = event.action,
            repeatCount = event.repeatCount,
            deviceId = event.deviceId,
            eventTime = event.eventTime
        )
        _lastRawKeyEvent.value = details

        // Dynamic Key Learning Mode:
        // If learning mode is active, pass the hardware codes directly to the learning listener
        val learningCallback = activeKeyLearningListener
        if (learningCallback != null) {
            learningCallback(event.keyCode, event.scanCode)
            // Consume key event while learning so it doesn't leak to other apps
            return true
        }

        // Match against learned key identifier or default CMF Phone 2 Pro Essential Key
        val matchesKey = isTargetKey(event.keyCode, event.scanCode)
        if (!matchesKey) {
            // Do not intercept volume, power, back, or unrelated hardware buttons
            return false
        }

        // Feed matching key event into gesture state machine
        return gestureClassifier?.processKeyEvent(event) ?: false
    }

    private fun isTargetKey(keyCode: Int, scanCode: Int): Boolean {
        // If user configured a specific key code/scan code, prioritize exact match
        if (targetKeyCode != 0 && keyCode == targetKeyCode) {
            return true
        }
        if (targetScanCode != 0 && scanCode == targetScanCode) {
            return true
        }
        // Default CMF Essential Key reference identifier fallback
        return (keyCode == 0 && scanCode == 250) || (keyCode == 0 && scanCode != 0)
    }

    private fun handleGestureRecognized(gesture: KeyGesture) {
        _lastRecognizedGesture.value = gesture

        when (gesture) {
            KeyGesture.SINGLE_PRESS -> {
                performHapticFeedback()
                serviceScope.launch {
                    val result = cycleRingerModeUseCase()
                    result.fold(
                        onSuccess = { newMode ->
                            _lastActionResult.value = "Cycled to ${newMode.displayName}"
                        },
                        onFailure = { error ->
                            _lastActionResult.value = "Error: ${error.localizedMessage}"
                        }
                    )
                }
            }
            KeyGesture.DOUBLE_PRESS -> {
                _lastActionResult.value = "Double Press (Unassigned)"
            }
            KeyGesture.TRIPLE_PRESS -> {
                _lastActionResult.value = "Triple Press (Unassigned)"
            }
            KeyGesture.LONG_PRESS -> {
                _lastActionResult.value = "Long Press (Unassigned)"
            }
        }
    }

    private fun performHapticFeedback() {
        if (!isHapticFeedbackEnabled) return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally empty. No screen or window monitoring requested.
    }

    override fun onInterrupt() {
        gestureClassifier?.reset()
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        gestureClassifier?.reset()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _lastRawKeyEvent = MutableStateFlow<RawKeyEventDetails?>(null)
        val lastRawKeyEvent: StateFlow<RawKeyEventDetails?> = _lastRawKeyEvent.asStateFlow()

        private val _lastRecognizedGesture = MutableStateFlow<KeyGesture?>(null)
        val lastRecognizedGesture: StateFlow<KeyGesture?> = _lastRecognizedGesture.asStateFlow()

        private val _lastActionResult = MutableStateFlow<String?>("Idle")
        val lastActionResult: StateFlow<String?> = _lastActionResult.asStateFlow()

        private val _savedKeyCodeFlow = MutableStateFlow(0)
        val savedKeyCodeFlow: StateFlow<Int> = _savedKeyCodeFlow.asStateFlow()

        private val _savedScanCodeFlow = MutableStateFlow(250)
        val savedScanCodeFlow: StateFlow<Int> = _savedScanCodeFlow.asStateFlow()

        @Volatile
        var activeKeyLearningListener: ((keyCode: Int, scanCode: Int) -> Unit)? = null
    }
}

