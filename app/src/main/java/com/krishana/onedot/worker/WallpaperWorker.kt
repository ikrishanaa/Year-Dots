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

            // Generate wallpaper (battery info no longer needed for display)
            val bitmap = WallpaperGenerator.generateBitmap(width, height, themeConfig)

            // Apply maximum quality settings like professional wallpaper apps
            // Fix quality issue: Use setStream with PNG at max quality instead of setBitmap
            // setBitmap() compresses the image, causing low quality
            // setStream() with PNG maintains original quality
            val stream = java.io.ByteArrayOutputStream()
            
            // Compress with PNG (lossless) at maximum quality
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            val inputStream = java.io.ByteArrayInputStream(stream.toByteArray())
            
            // Set quality hints for wallpaper manager
            // These hints ensure Android doesn't downscale or compress further
            wallpaperManager.suggestDesiredDimensions(width, height)
            
            // Set wallpaper ONLY on LOCK SCREEN with maximum quality settings
            // allowBackup=true, which=FLAG_LOCK for lock screen only
            wallpaperManager.setStream(inputStream, null, true, WallpaperManager.FLAG_LOCK)
            
            inputStream.close()
            stream.close()

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
