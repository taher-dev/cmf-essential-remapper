package com.monmoy.cmfessentialremapper.utils

import android.os.SystemClock
import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Recognized hardware key gestures.
 */
enum class KeyGesture(val title: String) {
    SINGLE_PRESS("Single Press"),
    DOUBLE_PRESS("Double Press"),
    TRIPLE_PRESS("Triple Press"),
    LONG_PRESS("Long Press")
}

/**
 * Classifies raw KeyEvent streams into high-level gesture actions:
 * Single press, Double press, Triple press, and Long press.
 *
 * Uses monotonic timestamps, ignores repeat events, ensures single press
 * waits long enough to differentiate from double/triple press, and prevents
 * long press from triggering single press.
 */
class KeyGestureClassifier(
    private val scope: CoroutineScope,
    var doublePressWindowMs: Long = 180L,
    var triplePressWindowMs: Long = 180L,
    var longPressThresholdMs: Long = 500L,
    private val onGestureRecognized: (KeyGesture) -> Unit
) {
    private var isKeyDown = false
    private var keyDownTime: Long = 0L
    private var clickCount = 0
    private var isLongPressTriggered = false
    private var longPressJob: Job? = null
    private var singlePressDebounceJob: Job? = null

    /**
     * Processes a hardware key event.
     * Returns true if the event was processed/consumed.
     */
    fun processKeyEvent(event: KeyEvent): Boolean {
        return processKeyEvent(event.action, event.repeatCount, event.eventTime)
    }

    /**
     * Overload accepting primitive values for unit testing independent of Android KeyEvent mock.
     */
    fun processKeyEvent(action: Int, repeatCount: Int = 0, eventTime: Long = 0L): Boolean {
        // Ignore hardware auto-repeat events when user holds the key down
        if (action == KeyEvent.ACTION_DOWN && repeatCount > 0) {
            return true
        }

        val now = if (eventTime > 0) eventTime else System.currentTimeMillis()

        when (action) {
            KeyEvent.ACTION_DOWN -> {
                if (isKeyDown) return true
                isKeyDown = true
                keyDownTime = now
                isLongPressTriggered = false

                // Stop pending multi-press evaluation while holding
                singlePressDebounceJob?.cancel()

                // Schedule long-press detection
                longPressJob?.cancel()
                longPressJob = scope.launch {
                    delay(longPressThresholdMs)
                    if (isKeyDown && !isLongPressTriggered) {
                        isLongPressTriggered = true
                        clickCount = 0
                        onGestureRecognized(KeyGesture.LONG_PRESS)
                    }
                }
                return true
            }

            KeyEvent.ACTION_UP -> {
                if (!isKeyDown) {
                    return true
                }
                isKeyDown = false
                longPressJob?.cancel()

                val pressDuration = now - keyDownTime

                // Discard UP event if long press already fired
                if (isLongPressTriggered || pressDuration >= longPressThresholdMs) {
                    isLongPressTriggered = false
                    clickCount = 0
                    return true
                }

                clickCount++

                if (clickCount == 1) {
                    singlePressDebounceJob?.cancel()
                    singlePressDebounceJob = scope.launch {
                        delay(doublePressWindowMs)
                        if (clickCount == 1) {
                            clickCount = 0
                            onGestureRecognized(KeyGesture.SINGLE_PRESS)
                        }
                    }
                } else if (clickCount == 2) {
                    singlePressDebounceJob?.cancel()
                    singlePressDebounceJob = scope.launch {
                        delay(triplePressWindowMs)
                        if (clickCount == 2) {
                            clickCount = 0
                            onGestureRecognized(KeyGesture.DOUBLE_PRESS)
                        }
                    }
                } else if (clickCount >= 3) {
                    singlePressDebounceJob?.cancel()
                    clickCount = 0
                    onGestureRecognized(KeyGesture.TRIPLE_PRESS)
                }
                return true
            }
        }
        return false
    }

    /**
     * Resets gesture classification state cleanly on service disconnect or rebind.
     */
    fun reset() {
        isKeyDown = false
        keyDownTime = 0L
        clickCount = 0
        isLongPressTriggered = false
        longPressJob?.cancel()
        singlePressDebounceJob?.cancel()
    }
}

