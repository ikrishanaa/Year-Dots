package com.krishana.onedot.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * Generates a 365-dot wallpaper representing the current year's progress.
 */
object WallpaperGenerator {

    data class ThemeConfig(
        val pastColor: Int,
        val todayColor: Int,
        val futureColor: Int,
        val backgroundColor: Int
    )

    /**
     * Generates a bitmap with 365 dots arranged in a 7-column grid.
     * 
     * @param width Target width in pixels
     * @param height Target height in pixels
     * @param themeConfig Color configuration
     * @return Generated wallpaper bitmap
     */
    fun generateBitmap(
        width: Int,
        height: Int,
        themeConfig: ThemeConfig
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
        canvas.drawColor(themeConfig.backgroundColor)

        val today = LocalDate.now()
        val yearStart = LocalDate.of(today.year, 1, 1)
        val daysInYear = if (today.isLeapYear) 366 else 365
        val currentDayOfYear = ChronoUnit.DAYS.between(yearStart, today).toInt() + 1

        // Grid layout: 7 columns (days of week), ~53 rows
        val columns = 7
        val rows = (daysInYear + columns - 1) / columns // Ceiling division

        // Calculate dot size and spacing
        val horizontalPadding = width * 0.05f
        val verticalPadding = height * 0.05f
        val availableWidth = width - (2 * horizontalPadding)
        val availableHeight = height - (2 * verticalPadding)

        val cellWidth = availableWidth / columns
        val cellHeight = availableHeight / rows
        val cellSize = min(cellWidth, cellHeight)
        val dotRadius = cellSize * 0.35f // 70% of cell width for dot diameter

        // Center the grid
        val gridWidth = columns * cellSize
        val gridHeight = rows * cellSize
        val startX = (width - gridWidth) / 2f
        val startY = (height - gridHeight) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = themeConfig.todayColor
        }

        // Draw dots
        for (day in 1..daysInYear) {
            val row = (day - 1) / columns
            val col = (day - 1) % columns

            val centerX = startX + (col * cellSize) + (cellSize / 2f)
            val centerY = startY + (row * cellSize) + (cellSize / 2f)

            when {
                day < currentDayOfYear -> {
                    // Past days - filled
                    paint.color = themeConfig.pastColor
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                }
                day == currentDayOfYear -> {
                    // Today - highlighted with border
                    paint.color = themeConfig.todayColor
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                    // Add white border
                    strokePaint.color = android.graphics.Color.WHITE
                    canvas.drawCircle(centerX, centerY, dotRadius + 2f, strokePaint)
                }
                else -> {
                    // Future days - outline only
                    paint.color = themeConfig.futureColor
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                    paint.style = Paint.Style.FILL // Reset
                }
            }
        }

        return bitmap
    }
}
