package com.krishana.onedot

import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krishana.onedot.core.WallpaperGenerator
import com.krishana.onedot.data.SettingsRepository
import com.krishana.onedot.ui.theme.OneDotTheme
import com.krishana.onedot.util.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Apply wallpaper immediately and synchronously
 */
suspend fun applyWallpaperNow(context: Context, repository: SettingsRepository) {
    withContext(Dispatchers.IO) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        
        // Get desired dimensions
        val width = wallpaperManager.desiredMinimumWidth
        val height = wallpaperManager.desiredMinimumHeight
        
        // Suggest dimensions to prevent downscaling
        wallpaperManager.suggestDesiredDimensions(width, height)
        
        // Get current settings
        val pastColor = repository.getPastColor()
        val todayColor = repository.getTodayColor()
        val futureColor = repository.getFutureColor()
        val backgroundColor = repository.getBackgroundColor()
        
        val themeConfig = WallpaperGenerator.ThemeConfig(
            pastColor = pastColor,
            todayColor = todayColor,
            futureColor = futureColor,
            backgroundColor = backgroundColor
        )
        
        // Generate bitmap
        val bitmap = WallpaperGenerator.generateBitmap(width, height, themeConfig)
        
        // Convert to PNG with maximum quality
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        val inputStream = outputStream.toByteArray().inputStream()
        
        // Set wallpaper ONLY on LOCK SCREEN
        wallpaperManager.setStream(inputStream, null, true, WallpaperManager.FLAG_LOCK)
        
        inputStream.close()
        bitmap.recycle()
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule daily update on first launch
        WorkScheduler.scheduleDailyWallpaperUpdate(this)
        
        setContent {
            OneDotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    // Collect saved colors from DataStore
    val savedPastColor by repository.pastColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_PAST_COLOR)
    val savedTodayColor by repository.todayColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_TODAY_COLOR)
    val savedFutureColor by repository.futureColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_FUTURE_COLOR)
    val savedBackgroundColor by repository.backgroundColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_BACKGROUND_COLOR)

    // Pending colors (not saved yet)
    var pendingPastColor by remember { mutableStateOf<Int?>(null) }
    var pendingTodayColor by remember { mutableStateOf<Int?>(null) }
    var pendingFutureColor by remember { mutableStateOf<Int?>(null) }
    var pendingBackgroundColor by remember { mutableStateOf<Int?>(null) }

    // Color picker dialogs
    var showPastColorPicker by remember { mutableStateOf(false) }
    var showTodayColorPicker by remember { mutableStateOf(false) }
    var showFutureColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    
    // Confirmation dialog
    var showSaveDialog by remember { mutableStateOf(false) }

    // Get current colors (pending or saved)
    val currentPastColor = pendingPastColor ?: savedPastColor
    val currentTodayColor = pendingTodayColor ?: savedTodayColor
    val currentFutureColor = pendingFutureColor ?: savedFutureColor
    val currentBackgroundColor = pendingBackgroundColor ?: savedBackgroundColor

    // Check if there are unsaved changes
    val hasChanges = pendingPastColor != null || 
                     pendingTodayColor != null || 
                     pendingFutureColor != null || 
                     pendingBackgroundColor != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Year Dots",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Live Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Mini preview grid (7x7)
                WallpaperPreview(
                    pastColor = Color(currentPastColor),
                    todayColor = Color(currentTodayColor),
                    futureColor = Color(currentFutureColor),
                    backgroundColor = Color(currentBackgroundColor)
                )
            }
        }

        // Color Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Color Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ColorSettingRow(
                    label = "Past Days",
                    color = Color(currentPastColor),
                    onClick = { showPastColorPicker = true }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ColorSettingRow(
                    label = "Current Day",
                    color = Color(currentTodayColor),
                    onClick = { showTodayColorPicker = true }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ColorSettingRow(
                    label = "Future Days",
                    color = Color(currentFutureColor),
                    onClick = { showFutureColorPicker = true }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ColorSettingRow(
                    label = "Background",
                    color = Color(currentBackgroundColor),
                    onClick = { showBackgroundColorPicker = true }
                )
            }
        }

        // Unsaved changes indicator
        if (hasChanges) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "⚠️ You have unsaved changes",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save & Apply Button
        Button(
            onClick = { showSaveDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasChanges
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & Apply to Lockscreen")
        }

        // Reset to Defaults Button
        OutlinedButton(
            onClick = {
                pendingPastColor = SettingsRepository.DEFAULT_PAST_COLOR
                pendingTodayColor = SettingsRepository.DEFAULT_TODAY_COLOR
                pendingFutureColor = SettingsRepository.DEFAULT_FUTURE_COLOR
                pendingBackgroundColor = SettingsRepository.DEFAULT_BACKGROUND_COLOR
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Defaults")
        }
    }

    // Color Picker Dialogs
    if (showPastColorPicker) {
        ImprovedColorPickerDialog(
            title = "Past Days Color",
            currentColor = Color(currentPastColor),
            onDismiss = { showPastColorPicker = false },
            onColorSelected = { color ->
                pendingPastColor = color.toArgb()
                showPastColorPicker = false
            }
        )
    }

    if (showTodayColorPicker) {
        ImprovedColorPickerDialog(
            title = "Current Day Color",
            currentColor = Color(currentTodayColor),
            onDismiss = { showTodayColorPicker = false },
            onColorSelected = { color ->
                pendingTodayColor = color.toArgb()
                showTodayColorPicker = false
            }
        )
    }

    if (showFutureColorPicker) {
        ImprovedColorPickerDialog(
            title = "Future Days Color",
            currentColor = Color(currentFutureColor),
            onDismiss = { showFutureColorPicker = false },
            onColorSelected = { color ->
                pendingFutureColor = color.toArgb()
                showFutureColorPicker = false
            }
        )
    }

    if (showBackgroundColorPicker) {
        ImprovedColorPickerDialog(
            title = "Background Color",
            currentColor = Color(currentBackgroundColor),
            onDismiss = { showBackgroundColorPicker = false },
            onColorSelected = { color ->
                pendingBackgroundColor = color.toArgb()
                showBackgroundColorPicker = false
            }
        )
    }

    // Save Confirmation Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Apply Settings?") },
            text = { 
                Text("This will save your color changes and apply them to your lock screen wallpaper.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                // Save pending colors
                                pendingPastColor?.let { repository.updatePastColor(it) }
                                pendingTodayColor?.let { repository.updateTodayColor(it) }
                                pendingFutureColor?.let { repository.updateFutureColor(it) }
                                pendingBackgroundColor?.let { repository.updateBackgroundColor(it) }
                                
                                // Apply wallpaper immediately (synchronous)
                                applyWallpaperNow(context, repository)
                                
                                // Clear pending changes
                                pendingPastColor = null
                                pendingTodayColor = null
                                pendingFutureColor = null
                                pendingBackgroundColor = null
                                
                                showSaveDialog = false
                                
                                Toast.makeText(
                                    context,
                                    "✓ Settings applied to lock screen!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                showSaveDialog = false
                                Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WallpaperPreview(
    pastColor: Color,
    todayColor: Color,
    futureColor: Color,
    backgroundColor: Color
) {
    // Mini 7x7 grid preview
    val totalDots = 49
    val currentDay = 31 // Example: Jan 31
    
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
            repeat(7) { row ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    repeat(7) { col ->
                        val dotNumber = row * 7 + col + 1
                        val dotColor = when {
                            dotNumber < currentDay -> pastColor
                            dotNumber == currentDay -> todayColor
                            else -> futureColor
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSettingRow(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun ImprovedColorPickerDialog(
    title: String,
    currentColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val presetColors = listOf(
        // Whites & Grays
        Color(0xFFFFFFFF), Color(0xFFCCCCCC), Color(0xFF999999), Color(0xFF666666),
        Color(0xFF333333), Color(0xFF000000),
        // Primary Colors
        Color(0xFFFF0000), Color(0xFF00FF00), Color(0xFF0000FF),
        // Oranges
        Color(0xFFFF6B35), Color(0xFFFF9800), Color(0xFFFFB74D),
        // Blues
        Color(0xFF2196F3), Color(0xFF1976D2), Color(0xFF0D47A1),
        // Greens
        Color(0xFF4CAF50), Color(0xFF388E3C), Color(0xFF1B5E20),
        // Purples
        Color(0xFF9C27B0), Color(0xFF7B1FA2), Color(0xFF4A148C),
        // Others
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFFFFEB3B),
        Color(0xFF00BCD4), Color(0xFF795548), Color(0xFF607D8B)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "Select a color:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                presetColors.chunked(6).forEach { rowColors ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
