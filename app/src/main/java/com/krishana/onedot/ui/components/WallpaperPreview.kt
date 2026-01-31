package com.krishana.onedot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun WallpaperPreview(
    pastColor: Color,
    todayColor: Color,
    futureColor: Color,
    backgroundColor: Color,
    dotShape: String = "dot",
    dotDensity: Int = 2 // 0=Tiny, 1=Small, 2=Medium, 3=Large
) {
    // 15x15 grid preview (225 dots)
    val totalDots = 15 * 15
    val currentDay = 113 // Middle of the grid to show progress clearly
    
    // Calculate dot size based on density
    // For 15x15 grid, available width is ~21dp per cell.
    // We can go larger to fill the space better.
    val dotSize = when (dotDensity) {
        0 -> 9.dp   // Tiny (was 4.dp)
        1 -> 12.dp   // Small (was 6.dp)
        2 -> 15.dp  // Medium (was 8.dp)
        3 -> 18.dp  // Large (was 10.dp)
        else -> 12.dp
    }
    
    // Get shape object based on dotShape name
    val getShape: (String) -> Shape = { shapeName ->
        when (shapeName) {
            "dot", "circle" -> CircleShape
            "rounded" -> RoundedCornerShape(30) // 30% rounding
            "square" -> RoundedCornerShape(15)  // 15% rounding
            "pill" -> RoundedCornerShape(50)    // Fully rounded for pill
            else -> CircleShape
        }
    }
    
    val shape = getShape(dotShape)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(backgroundColor, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            repeat(15) { row ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(15) { col ->
                        val dotNumber = row * 15 + col + 1
                        val dotColor = when {
                            dotNumber < currentDay -> pastColor
                            dotNumber == currentDay -> todayColor
                            else -> futureColor
                        }
                        
                        // Modifier for size and shape
                        val baseModifier = if (dotShape == "pill") {
                            Modifier
                                .width(dotSize * 1.8f) // Wider for pill
                                .height(dotSize * 0.9f) // Slightly shorter
                        } else {
                            Modifier.size(dotSize)
                        }
                        
                        Box(
                            modifier = baseModifier.background(dotColor, shape)
                        )
                    }
                }
            }
        }
    }
}
