package com.monmoy.cmfessentialremapper.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

/**
 * BroadcastReceiver for system ringer mode, volume, and notification interruption state changes.
 */
class RingerModeReceiver(
    private val onStateChanged: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
            onStateChanged()
        }
    }
}
