package com.krishana.onedot

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krishana.onedot.data.SettingsRepository
import com.krishana.onedot.ui.theme.OneDotTheme
import com.krishana.onedot.util.WorkScheduler
import kotlinx.coroutines.launch

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

    // Collect colors from DataStore
    val pastColor by repository.pastColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_PAST_COLOR)
    val todayColor by repository.todayColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_TODAY_COLOR)
    val futureColor by repository.futureColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_FUTURE_COLOR)
    val backgroundColor by repository.backgroundColorFlow.collectAsState(initial = SettingsRepository.DEFAULT_BACKGROUND_COLOR)

    var showPastColorPicker by remember { mutableStateOf(false) }
    var showTodayColorPicker by remember { mutableStateOf(false) }
    var showFutureColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Past Days Color
        ColorPickerRow(
            label = stringResource(R.string.past_days_color),
            color = Color(pastColor),
            onClick = { showPastColorPicker = true }
        )

        // Today Color
        ColorPickerRow(
            label = stringResource(R.string.today_color),
            color = Color(todayColor),
            onClick = { showTodayColorPicker = true }
        )

        // Future Days Color
        ColorPickerRow(
            label = stringResource(R.string.future_days_color),
            color = Color(futureColor),
            onClick = { showFutureColorPicker = true }
        )

        // Background Color
        ColorPickerRow(
            label = stringResource(R.string.background_color),
            color = Color(backgroundColor),
            onClick = { showBackgroundColorPicker = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Apply Now Button
        Button(
            onClick = {
                scope.launch {
                    WorkScheduler.triggerImmediateUpdate(context)
                    Toast.makeText(
                        context,
                        context.getString(R.string.wallpaper_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply_now))
        }
    }

    // Color Picker Dialogs (Simple version - in production, use a proper color picker library)
    if (showPastColorPicker) {
        SimpleColorPickerDialog(
            currentColor = Color(pastColor),
            onDismiss = { showPastColorPicker = false },
            onColorSelected = { color ->
                scope.launch {
                    repository.updatePastColor(color.toArgb())
                }
                showPastColorPicker = false
            }
        )
    }

    if (showTodayColorPicker) {
        SimpleColorPickerDialog(
            currentColor = Color(todayColor),
            onDismiss = { showTodayColorPicker = false },
            onColorSelected = { color ->
                scope.launch {
                    repository.updateTodayColor(color.toArgb())
                }
                showTodayColorPicker = false
            }
        )
    }

    if (showFutureColorPicker) {
        SimpleColorPickerDialog(
            currentColor = Color(futureColor),
            onDismiss = { showFutureColorPicker = false },
            onColorSelected = { color ->
                scope.launch {
                    repository.updateFutureColor(color.toArgb())
                }
                showFutureColorPicker = false
            }
        )
    }

    if (showBackgroundColorPicker) {
        SimpleColorPickerDialog(
            currentColor = Color(backgroundColor),
            onDismiss = { showBackgroundColorPicker = false },
            onColorSelected = { color ->
                scope.launch {
                    repository.updateBackgroundColor(color.toArgb())
                }
                showBackgroundColorPicker = false
            }
        )
    }
}

@Composable
fun ColorPickerRow(
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
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun SimpleColorPickerDialog(
    currentColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val presetColors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Green
        Color(0xFFF44336), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00BCD4), // Cyan
        Color(0xFF795548), // Brown
        Color(0xFF424242), // Dark Grey
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            Column {
                presetColors.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(color, CircleShape)
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
