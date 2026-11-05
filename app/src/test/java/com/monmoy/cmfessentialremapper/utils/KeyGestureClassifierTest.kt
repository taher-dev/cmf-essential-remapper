package com.monmoy.cmfessentialremapper.utils

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyGestureClassifierTest {

    private fun createKeyEvent(action: Int, keyCode: Int = 0, scanCode: Int = 250, repeatCount: Int = 0, eventTime: Long = 1000L): KeyEvent {
        return KeyEvent(
            1000L,
            eventTime,
            action,
            keyCode,
            repeatCount,
            0,
            0,
            scanCode
        )
    }

    @Test
    fun testSinglePressRecognizedAfterDoublePressWindow() = runBlocking {
        var recognizedGesture: KeyGesture? = null
        val classifier = KeyGestureClassifier(
            scope = this,
            doublePressWindowMs = 30L,
            longPressThresholdMs = 100L
        ) { gesture ->
            recognizedGesture = gesture
        }

        // Simulate physical press: DOWN then UP within 10ms
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1000L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1010L))

        // Before window expires, gesture should not have fired yet
        assertEquals(null, recognizedGesture)

        // Wait past double press window
        delay(50L)

        assertEquals(KeyGesture.SINGLE_PRESS, recognizedGesture)
    }

    @Test
    fun testDoublePressRecognized() = runBlocking {
        var recognizedGesture: KeyGesture? = null
        val classifier = KeyGestureClassifier(
            scope = this,
            doublePressWindowMs = 40L,
            triplePressWindowMs = 40L,
            longPressThresholdMs = 150L
        ) { gesture ->
            recognizedGesture = gesture
        }

        // First press
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1000L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1010L))

        // Second press before timeout
        delay(15L)
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1025L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1035L))

        // Wait past triple press window
        delay(60L)

        assertEquals(KeyGesture.DOUBLE_PRESS, recognizedGesture)
    }

    @Test
    fun testTriplePressRecognizedImmediately() = runBlocking {
        var recognizedGesture: KeyGesture? = null
        val classifier = KeyGestureClassifier(
            scope = this,
            doublePressWindowMs = 50L,
            triplePressWindowMs = 50L,
            longPressThresholdMs = 200L
        ) { gesture ->
            recognizedGesture = gesture
        }

        // 3 rapid clicks
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1000L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1010L))
        delay(10L)
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1020L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1030L))
        delay(10L)
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1040L))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1050L))

        delay(10L)
        assertEquals(KeyGesture.TRIPLE_PRESS, recognizedGesture)
    }

    @Test
    fun testLongPressDoesNotTriggerSinglePress() = runBlocking {
        val gestures = mutableListOf<KeyGesture>()
        val classifier = KeyGestureClassifier(
            scope = this,
            doublePressWindowMs = 30L,
            longPressThresholdMs = 40L
        ) { gesture ->
            gestures.add(gesture)
        }

        // Key down and held past threshold
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, eventTime = 1000L))
        delay(60L)

        // Key released after threshold
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_UP, eventTime = 1060L))
        delay(50L)

        // Only LONG_PRESS should fire; SINGLE_PRESS must NOT fire
        assertEquals(1, gestures.size)
        assertEquals(KeyGesture.LONG_PRESS, gestures.first())
    }

    @Test
    fun testRepeatedActionDownIgnored() = runBlocking {
        var actionCount = 0
        val classifier = KeyGestureClassifier(
            scope = this,
            doublePressWindowMs = 30L,
            longPressThresholdMs = 100L
        ) {
            actionCount++
        }

        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, repeatCount = 0))
        // Hardware auto-repeats while held
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, repeatCount = 1))
        classifier.processKeyEvent(createKeyEvent(KeyEvent.ACTION_DOWN, repeatCount = 2))

        classifier.reset()
        assertEquals(0, actionCount)
    }
}

