package com.monmoy.cmfessentialremapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepositoryImpl
import com.monmoy.cmfessentialremapper.domain.model.KeyAction
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.domain.usecase.CycleRingerModeUseCase
import com.monmoy.cmfessentialremapper.domain.usecase.RingerCycleOrder
import com.monmoy.cmfessentialremapper.presentation.overlay.OverlayIcon
import com.monmoy.cmfessentialremapper.presentation.overlay.OverlayState
import com.monmoy.cmfessentialremapper.presentation.overlay.StateOverlayManager
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
    private lateinit var ringerRepository: RingerModeRepository
    private lateinit var cycleRingerModeUseCase: CycleRingerModeUseCase
    private var gestureClassifier: KeyGestureClassifier? = null

    private var targetKeyCode: Int = 0
    private var targetScanCode: Int = 250
    private var isHapticFeedbackEnabled: Boolean = true

    private var singlePressAction: KeyAction = KeyAction.CYCLE_RINGER
    private var doublePressAction: KeyAction = KeyAction.NONE
    private var longPressAction: KeyAction = KeyAction.NONE

    private var isTorchOn: Boolean = false
    private var torchCameraId: String? = null

    private lateinit var stateOverlayManager: StateOverlayManager
    private var isVisualOverlayEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(applicationContext)
        ringerRepository = RingerModeRepositoryImpl(applicationContext, dataStoreManager)
        cycleRingerModeUseCase = CycleRingerModeUseCase(ringerRepository)
        stateOverlayManager = StateOverlayManager(this)

        gestureClassifier = KeyGestureClassifier(
            scope = serviceScope,
            onGestureRecognized = ::handleGestureRecognized
        )

        initCameraManager()
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
            launch {
                dataStoreManager.singlePressAction.collect { action ->
                    singlePressAction = action
                }
            }
            launch {
                dataStoreManager.doublePressAction.collect { action ->
                    doublePressAction = action
                }
            }
            launch {
                dataStoreManager.longPressAction.collect { action ->
                    longPressAction = action
                }
            }
            launch {
                dataStoreManager.isVisualOverlayEnabled.collect { enabled ->
                    isVisualOverlayEnabled = enabled
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

        // Match against learned key identifier or default CMF Essential Key
        val matchesKey = isTargetKey(event.keyCode, event.scanCode)
        if (!matchesKey) {
            // Do not intercept volume, power, back, or unrelated hardware buttons
            return false
        }

        // Feed matching key event into gesture state machine
        gestureClassifier?.processKeyEvent(event)
        // Consume both DOWN and UP so the key never leaks to Nothing OS or active apps
        return true
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
        return scanCode == 250 || (keyCode == 0 && scanCode != 0)
    }

    private fun handleGestureRecognized(gesture: KeyGesture) {
        _lastRecognizedGesture.value = gesture
        val action = when (gesture) {
            KeyGesture.SINGLE_PRESS -> singlePressAction
            KeyGesture.DOUBLE_PRESS -> doublePressAction
            KeyGesture.LONG_PRESS -> longPressAction
            KeyGesture.TRIPLE_PRESS -> KeyAction.NONE
        }

        if (action == KeyAction.NONE) {
            _lastActionResult.value = "No action assigned for ${gesture.title}"
            return
        }

        performHapticFeedback()
        executeAction(action)
    }

    private fun executeAction(action: KeyAction) {
        serviceScope.launch {
            when (action) {
                KeyAction.CYCLE_RINGER -> {
                    val result = cycleRingerModeUseCase(RingerCycleOrder.RING_VIBRATE_SILENT)
                    result.fold(
                        onSuccess = { newMode ->
                            _lastActionResult.value = "Cycled to ${newMode.displayName}"
                            showOverlayForRingerMode(newMode)
                        },
                        onFailure = { error ->
                            _lastActionResult.value = "Error: ${error.localizedMessage}"
                        }
                    )
                }
                KeyAction.CYCLE_RINGER_2 -> {
                    val result = cycleRingerModeUseCase(RingerCycleOrder.RING_SILENT_VIBRATE)
                    result.fold(
                        onSuccess = { newMode ->
                            _lastActionResult.value = "Cycled to ${newMode.displayName}"
                            showOverlayForRingerMode(newMode)
                        },
                        onFailure = { error ->
                            _lastActionResult.value = "Error: ${error.localizedMessage}"
                        }
                    )
                }
                KeyAction.TOGGLE_FLASHLIGHT -> {
                    toggleFlashlight()
                    if (isVisualOverlayEnabled) {
                        stateOverlayManager.show(
                            OverlayState(
                                title = if (isTorchOn) "TORCH ON" else "TORCH OFF",
                                subtitle = if (isTorchOn) "Flashlight active" else "Flashlight inactive",
                                icon = if (isTorchOn) OverlayIcon.FLASHLIGHT_ON else OverlayIcon.FLASHLIGHT_OFF,
                                accentColor = if (isTorchOn) 0xFFFFA000.toInt() else 0xFF888888.toInt()
                            )
                        )
                    }
                }
                KeyAction.TAKE_SCREENSHOT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                        _lastActionResult.value = "Screenshot captured"
                        if (isVisualOverlayEnabled) {
                            stateOverlayManager.show(
                                OverlayState(
                                    title = "SCREENSHOT",
                                    subtitle = "Captured",
                                    icon = OverlayIcon.SCREENSHOT,
                                    accentColor = 0xFFEEEEEE.toInt()
                                )
                            )
                        }
                    } else {
                        _lastActionResult.value = "Screenshot requires Android 9+"
                    }
                }
                KeyAction.OPEN_NOTIFICATIONS -> {
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                    _lastActionResult.value = "Notifications opened"
                    if (isVisualOverlayEnabled) {
                        stateOverlayManager.show(
                            OverlayState(
                                title = "NOTIFICATIONS",
                                subtitle = "Expanded",
                                icon = OverlayIcon.NOTIFICATIONS,
                                accentColor = 0xFFEEEEEE.toInt()
                            )
                        )
                    }
                }
                KeyAction.OPEN_QUICK_SETTINGS -> {
                    performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                    _lastActionResult.value = "Quick Settings opened"
                    if (isVisualOverlayEnabled) {
                        stateOverlayManager.show(
                            OverlayState(
                                title = "QUICK SETTINGS",
                                subtitle = "Expanded",
                                icon = OverlayIcon.QUICK_SETTINGS,
                                accentColor = 0xFFEEEEEE.toInt()
                            )
                        )
                    }
                }
                KeyAction.LOCK_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                        _lastActionResult.value = "Screen locked"
                        if (isVisualOverlayEnabled) {
                            stateOverlayManager.show(
                                OverlayState(
                                    title = "SCREEN LOCKED",
                                    subtitle = null,
                                    icon = OverlayIcon.LOCK,
                                    accentColor = 0xFFD71921.toInt()
                                )
                            )
                        }
                    } else {
                        _lastActionResult.value = "Lock Screen requires Android 9+"
                    }
                }
                KeyAction.NONE -> {}
            }
        }
    }

    private fun showOverlayForRingerMode(mode: RingerMode) {
        if (!isVisualOverlayEnabled) return
        val state = when (mode) {
            RingerMode.RING -> OverlayState(
                title = "RING",
                subtitle = "Calls & alerts ring",
                icon = OverlayIcon.RING,
                accentColor = 0xFFEEEEEE.toInt()
            )
            RingerMode.VIBRATE -> OverlayState(
                title = "VIBRATE",
                subtitle = "Calls & alerts vibrate",
                icon = OverlayIcon.VIBRATE,
                accentColor = 0xFFCCCCCC.toInt()
            )
            RingerMode.SILENT -> OverlayState(
                title = "SILENT",
                subtitle = "Muted",
                icon = OverlayIcon.SILENT,
                accentColor = 0xFFD71921.toInt() // Nothing Red
            )
        }
        stateOverlayManager.show(state)
    }

    private fun initCameraManager() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
            torchCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull()

            cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == torchCameraId) {
                        isTorchOn = enabled
                    }
                }
            }, null)
        } catch (_: Throwable) {}
    }

    private fun toggleFlashlight() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val id = torchCameraId ?: cameraManager?.cameraIdList?.firstOrNull()
            if (id == null) {
                _lastActionResult.value = "Flashlight unavailable"
                return
            }
            isTorchOn = !isTorchOn
            cameraManager?.setTorchMode(id, isTorchOn)
            _lastActionResult.value = if (isTorchOn) "Flashlight ON" else "Flashlight OFF"
        } catch (e: Exception) {
            _lastActionResult.value = "Flashlight error: ${e.localizedMessage}"
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
        stateOverlayManager.destroy()
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

