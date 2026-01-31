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

    data class BatteryInfo(
        val percentage: Int,
        val isCharging: Boolean,
        val isFull: Boolean,
        val isLow: Boolean
    )

    /**
     * Generates a bitmap with 364 dots arranged in a 13-column grid.
     * 
     * @param width Target width in pixels
     * @param height Target height in pixels
     * @param themeConfig Color configuration
     * @param batteryInfo Optional battery status information
     * @return Generated wallpaper bitmap
     */
    fun generateBitmap(
        width: Int,
        height: Int,
        themeConfig: ThemeConfig,
        batteryInfo: BatteryInfo? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
        canvas.drawColor(themeConfig.backgroundColor)

        val today = LocalDate.now()
        val yearStart = LocalDate.of(today.year, 1, 1)
        val daysInYear = if (today.isLeapYear) 366 else 365
        val currentDayOfYear = ChronoUnit.DAYS.between(yearStart, today).toInt() + 1
        val daysLeft = daysInYear - currentDayOfYear
        val percent = ((currentDayOfYear.toFloat() / daysInYear) * 100).toInt()

        // Grid layout: 365 dots total for full year
        val columns = 13
        val totalDots = 365 // Full year (365 days)
        val rows = (totalDots + columns - 1) / columns // 29 rows (ceiling division)

        // Padding - Compact minimal look
        val topPadding = height * 0.28f  // Space for clock
        val bottomPadding = height * 0.12f  // Space for bottom text and controls
        val sidePadding = width * 0.08f 
        
        val availableWidth = width - (2 * sidePadding)
        val availableHeight = height - topPadding - bottomPadding
        
        // Calculate cell size based on width
        val cellWidth = availableWidth / columns
        
        // Calculate cell height to fit all rows
        val cellHeight = availableHeight / rows
        
        // Use the smaller dimension to ensure everything fits
        val cellSize = min(cellWidth, cellHeight)
        
        // Much smaller dots with gap spacing (28% of cell + implicit gap from grid)
        val dotRadius = cellSize * 0.28f
        
        // Recalculate actual grid dimensions
        val totalGridWidth = cellSize * columns
        val totalGridHeight = cellSize * rows
        
        // Center the grid
        val startX = (width - totalGridWidth) / 2f
        val startY = topPadding + (availableHeight - totalGridHeight) / 2f

        val paint = Paint().apply {
            isAntiAlias = true              // Smooth edges
            isDither = true                 // Better color gradients
            isFilterBitmap = true           // High-quality bitmap filtering
            style = Paint.Style.FILL
        }

        // Draw dots (fixed 364-dot grid)
        for (day in 1..totalDots) {
            val row = (day - 1) / columns
            val col = (day - 1) % columns

            val centerX = startX + (col * cellSize) + (cellSize / 2f)
            val centerY = startY + (row * cellSize) + (cellSize / 2f)

            when {
                day < currentDayOfYear -> {
                    // Past: Filled White
                    paint.color = themeConfig.pastColor
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                }
                day == currentDayOfYear -> {
                    // Today: Filled with subtle glow effect for visual prominence
                    paint.color = themeConfig.todayColor
                    
                    // Add subtle outer glow
                    val glowPaint = Paint().apply {
                        isAntiAlias = true
                        isDither = true
                        isFilterBitmap = true
                        style = Paint.Style.FILL
                        color = themeConfig.todayColor
                        maskFilter = android.graphics.BlurMaskFilter(
                            dotRadius * 0.3f, 
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    canvas.drawCircle(centerX, centerY, dotRadius * 1.15f, glowPaint)
                    
                    // Draw main dot on top
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                }
                else -> {
                    // Future: Filled Dark Grey
                    paint.color = themeConfig.futureColor
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                }
            }
        }

        // Draw Bottom Text (Progress only, no battery) with maximum quality
        val textPaint = Paint().apply {
            isAntiAlias = true                  // Smooth edges
            isSubpixelText = true              // Sub-pixel rendering for sharper text
            isLinearText = true                // Don't scale text for better quality
            isDither = true                    // Better color rendering
            isFilterBitmap = true              // Filter when scaling
            color = 0xFFCCCCCC.toInt()        // Lighter grey for better visibility
            textSize = width * 0.025f          // Much smaller text
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        }

        val text = "$daysLeft days \u2022 $percent% Complete"
        
        // Position higher for better balance
        canvas.drawText(text, width / 2f, height * 0.88f, textPaint)

        return bitmap
    }
}
