package com.krishana.onedot.worker

import android.app.WallpaperManager
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishana.onedot.core.WallpaperGenerator
import com.krishana.onedot.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Background worker that updates the wallpaper daily at midnight.
 */
class WallpaperWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = SettingsRepository(applicationContext)
            
            // Get current colors from DataStore
            val pastColor = repository.pastColorFlow.first()
            val todayColor = repository.todayColorFlow.first()
            val futureColor = repository.futureColorFlow.first()
            val backgroundColor = repository.backgroundColorFlow.first()

            val themeConfig = WallpaperGenerator.ThemeConfig(
                pastColor = pastColor,
                todayColor = todayColor,
                futureColor = futureColor,
                backgroundColor = backgroundColor
            )

            // Get device screen dimensions
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val desiredWidth = wallpaperManager.desiredMinimumWidth
            val desiredHeight = wallpaperManager.desiredMinimumHeight

            // If wallpaper manager doesn't provide dimensions, fallback to display metrics
            val (width, height) = if (desiredWidth > 0 && desiredHeight > 0) {
                Pair(desiredWidth, desiredHeight)
            } else {
                val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getRealMetrics(metrics)
                Pair(metrics.widthPixels, metrics.heightPixels)
            }

            // Generate wallpaper
            val bitmap = WallpaperGenerator.generateBitmap(width, height, themeConfig)

            // Set wallpaper
            wallpaperManager.setBitmap(bitmap)

            // Update last update timestamp
            repository.updateLastUpdateTimestamp(System.currentTimeMillis())

            // Clean up
            bitmap.recycle()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
