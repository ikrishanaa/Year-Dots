package com.krishana.onedot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.krishana.onedot.util.WorkScheduler

/**
 * Receives BOOT_COMPLETED broadcast to reschedule work after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule the daily wallpaper update
            WorkScheduler.scheduleDailyWallpaperUpdate(context)
        }
    }
}
