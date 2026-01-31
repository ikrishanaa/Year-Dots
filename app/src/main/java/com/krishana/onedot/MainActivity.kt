package com.krishana.onedot

import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
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
        try {
            android.util.Log.d("YearDots", "=== WALLPAPER UPDATE START ===")
            
            // CRITICAL: Check permissions before attempting to set wallpaper
            android.util.Log.d("YearDots", "Checking SET_WALLPAPER permission...")
            val hasWallpaperPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.SET_WALLPAPER
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            android.util.Log.d("YearDots", "SET_WALLPAPER permission: $hasWallpaperPerm")
            
            if (!hasWallpaperPerm) {
                android.util.Log.e("YearDots", "ERROR: SET_WALLPAPER permission not granted!")
                throw SecurityException("SET_WALLPAPER permission not granted. Please enable it in app permissions.")
            }
            
            // Check storage permissions (needed on some devices)
            android.util.Log.d("YearDots", "Checking storage permissions...")
            android.util.Log.d("YearDots", "Android SDK: ${android.os.Build.VERSION.SDK_INT}")
            
            val hasStoragePerm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val perm = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_IMAGES
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                android.util.Log.d("YearDots", "READ_MEDIA_IMAGES permission: $perm")
                perm
            } else {
                val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasWrite = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                android.util.Log.d("YearDots", "READ_EXTERNAL_STORAGE: $hasRead, WRITE_EXTERNAL_STORAGE: $hasWrite")
                hasRead && hasWrite
            }
            
            if (!hasStoragePerm) {
                android.util.Log.e("YearDots", "ERROR: Storage permissions not granted!")
                throw SecurityException("Storage permissions not granted. Please enable them in app settings.")
            }
            
            android.util.Log.d("YearDots", "All permissions OK, getting WallpaperManager...")
            val wallpaperManager = WallpaperManager.getInstance(context)
            
            // Get ACTUAL screen dimensions for pixel-perfect quality
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            // Use 1.5x super-sampling for even sharper quality
            val width = (screenWidth * 1.5f).toInt()
            val height = (screenHeight * 1.5f).toInt()
            
            android.util.Log.d("YearDots", "Screen dimensions: ${screenWidth}x${screenHeight}")
            android.util.Log.d("YearDots", "Wallpaper dimensions (1.5x super-sampled): ${width}x${height}")
            
            // Suggest dimensions to prevent downscaling (optional - requires SET_WALLPAPER_HINTS permission)
            try {
                wallpaperManager.suggestDesiredDimensions(width, height)
                android.util.Log.d("YearDots", "Successfully suggested dimensions: ${width}x${height}")
            } catch (e: SecurityException) {
                android.util.Log.w("YearDots", "Cannot suggest dimensions (missing SET_WALLPAPER_HINTS permission), continuing anyway...")
            }
            
            // Get current settings
            android.util.Log.d("YearDots", "Loading color settings...")
            val pastColor = repository.getPastColor()
            val todayColor = repository.getTodayColor()
            val futureColor = repository.getFutureColor()
            val backgroundColor = repository.getBackgroundColor()
            val dotShape = repository.getDotShape()
            
            android.util.Log.d("YearDots", "Colors - Past: 0x${pastColor.toString(16)}, Today: 0x${todayColor.toString(16)}, Future: 0x${futureColor.toString(16)}, BG: 0x${backgroundColor.toString(16)}, Shape: $dotShape")
            
            val themeConfig = WallpaperGenerator.ThemeConfig(
                pastColor = pastColor,
                todayColor = todayColor,
                futureColor = futureColor,
                backgroundColor = backgroundColor,
                dotShape = dotShape
            )
            
            // Generate bitmap
            android.util.Log.d("YearDots", "Generating wallpaper bitmap...")
            val bitmap = WallpaperGenerator.generateBitmap(width, height, themeConfig)
            android.util.Log.d("YearDots", "Bitmap generated: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")
            
            try {
                // Use setBitmap with high-quality settings
                // allowWhileIdle=false ensures full quality processing
                android.util.Log.d("YearDots", "Setting wallpaper on LOCK SCREEN with high quality...")
                wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
                android.util.Log.d("YearDots", "✓ Wallpaper set successfully at ${width}x${height}!")
            } catch (e: SecurityException) {
                android.util.Log.e("YearDots", "SecurityException: ${e.message}", e)
                android.util.Log.e("YearDots", "Stack trace: ${e.stackTraceToString()}")
                throw SecurityException("Permission denied when setting wallpaper. Please ensure all permissions are granted in Settings > Apps > Year Dots > Permissions.", e)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("YearDots", "IllegalArgumentException: ${e.message}", e)
                throw Exception("Invalid wallpaper size or format. Please try again.", e)
            } catch (e: Exception) {
                android.util.Log.e("YearDots", "Unexpected error: ${e.message}", e)
                android.util.Log.e("YearDots", "Error type: ${e.javaClass.name}")
                android.util.Log.e("YearDots", "Stack trace: ${e.stackTraceToString()}")
                throw Exception("Failed to set wallpaper: ${e.message}", e)
            } finally {
                bitmap.recycle()
                android.util.Log.d("YearDots", "Bitmap recycled")
            }
        } catch (e: SecurityException) {
            // Re-throw security exceptions with clear message
            android.util.Log.e("YearDots", "Final SecurityException handler: ${e.message}", e)
            throw Exception("Permission Error: ${e.message}", e)
        } catch (e: Exception) {
            // Re-throw with context
            android.util.Log.e("YearDots", "Final error handler: ${e.message}", e)
            throw Exception("Wallpaper update failed: ${e.message}", e)
        } finally {
            android.util.Log.d("YearDots", "=== WALLPAPER UPDATE END ===")
        }
    }
}





class MainActivity : ComponentActivity() {
    
    // Permission state
    private var hasPermissions by mutableStateOf(false)
    
    // Permission launcher - must be registered before onCreate
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        hasPermissions = allGranted
        
        if (!allGranted) {
            Toast.makeText(
                this,
                "⚠️ Permissions are needed for the app to function properly",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initial check
        checkPermissions()
        
        if (!hasPermissions) {
            // Request on startup if not granted
            requestPermissions()
        }
        
        // Schedule daily update on first launch
        WorkScheduler.scheduleDailyWallpaperUpdate(this)
        
        setContent {
            OneDotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Permission Warning Banner
                        if (!hasPermissions) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⚠️ Permissions Needed",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Please grant all permissions to allow the wallpaper to update automatically.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { requestPermissions() }) {
                                        Text("Grant Permissions")
                                    }
                                }
                            }
                        }
                        
                        // Main Settings UI
                        SettingsScreen()
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-check permissions when user returns to app
        // (e.g., after granting permissions in Settings)
        checkPermissions()
    }
    
    private fun checkPermissions() {
        val permissions = getRequiredPermissions()
        hasPermissions = permissions.all {
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, it
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        val permissions = getRequiredPermissions()
        permissionLauncher.launch(permissions.toTypedArray())
    }
    
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // Core functionality
        permissions.add(android.Manifest.permission.SET_WALLPAPER)
        
        // Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 and below - need both READ and WRITE for wallpaper operations
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        return permissions
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
    val savedDotShape by repository.dotShapeFlow.collectAsState(initial = SettingsRepository.DEFAULT_DOT_SHAPE)

    // Pending colors (not saved yet)
    var pendingPastColor by remember { mutableStateOf<Int?>(null) }
    var pendingTodayColor by remember { mutableStateOf<Int?>(null) }
    var pendingFutureColor by remember { mutableStateOf<Int?>(null) }
    var pendingBackgroundColor by remember { mutableStateOf<Int?>(null) }
    var pendingDotShape by remember { mutableStateOf<String?>(null) }

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
    val currentDotShape = pendingDotShape ?: savedDotShape

    // Check if there are unsaved changes
    val hasChanges = pendingPastColor != null || 
                     pendingTodayColor != null || 
                     pendingFutureColor != null || 
                     pendingBackgroundColor != null ||
                     pendingDotShape != null

    var showDebugInfo by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Year Dots",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAboutDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(onClick = { showDebugInfo = !showDebugInfo }) {
                    Text(if (showDebugInfo) "Hide Debug" else "Show Debug")
                }
            }
        }
        
        // Debug Info Card
        if (showDebugInfo) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔍 Debug Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Android Version
                    DebugInfoRow("Android SDK", android.os.Build.VERSION.SDK_INT.toString())
                    DebugInfoRow("Android Version", android.os.Build.VERSION.RELEASE)
                    DebugInfoRow("Device", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Permissions
                    Text(
                        text = "Permissions:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val hasWallpaper = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.SET_WALLPAPER
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    DebugInfoRow("SET_WALLPAPER", if (hasWallpaper) "✅ Granted" else "❌ Denied", if (hasWallpaper) Color(0xFF4CAF50) else Color(0xFFF44336))
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val hasNotif = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        DebugInfoRow("POST_NOTIFICATIONS", if (hasNotif) "✅ Granted" else "❌ Denied", if (hasNotif) Color(0xFF4CAF50) else Color(0xFFF44336))
                        
                        val hasMedia = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_MEDIA_IMAGES
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        DebugInfoRow("READ_MEDIA_IMAGES", if (hasMedia) "✅ Granted" else "❌ Denied", if (hasMedia) Color(0xFF4CAF50) else Color(0xFFF44336))
                    } else {
                        val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        DebugInfoRow("READ_EXTERNAL_STORAGE", if (hasRead) "✅ Granted" else "❌ Denied", if (hasRead) Color(0xFF4CAF50) else Color(0xFFF44336))
                        
                        val hasWrite = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        DebugInfoRow("WRITE_EXTERNAL_STORAGE", if (hasWrite) "✅ Granted" else "❌ Denied", if (hasWrite) Color(0xFF4CAF50) else Color(0xFFF44336))
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Wallpaper Info
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    DebugInfoRow("Screen Width", "${wallpaperManager.desiredMinimumWidth}px")
                    DebugInfoRow("Screen Height", "${wallpaperManager.desiredMinimumHeight}px")
                    
                    Text(
                        text = "Tap 'Show Debug' again to hide this info",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

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

        // Dot Shape Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Shape",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Shape Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShapeOptionItem(
                        selected = currentDotShape == "circle",
                        label = "Dot",
                        onClick = { pendingDotShape = "circle" }
                    ) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                        )
                    }
                    
                    ShapeOptionItem(
                        selected = currentDotShape == "rounded",
                        label = "Rounded",
                        onClick = { pendingDotShape = "rounded" }
                    ) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        )
                    }
                    
                    ShapeOptionItem(
                        selected = currentDotShape == "square",
                        label = "Square",
                        onClick = { pendingDotShape = "square" }
                    ) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        )
                    }
                    
                    ShapeOptionItem(
                        selected = currentDotShape == "pill",
                        label = "Pill",
                        onClick = { pendingDotShape = "pill" }
                    ) { color ->
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 24.dp)
                                .background(color, CircleShape)
                        )
                    }
                }
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
                pendingDotShape = SettingsRepository.DEFAULT_DOT_SHAPE
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
                                pendingDotShape?.let { repository.updateDotShape(it) }
                                
                                // Apply wallpaper immediately (synchronous)
                                applyWallpaperNow(context, repository)
                                
                                // Clear pending changes
                                pendingPastColor = null
                                pendingTodayColor = null
                                pendingFutureColor = null
                                pendingBackgroundColor = null
                                pendingDotShape = null
                                
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
// This is a temporary file to append to MainActivity.kt

@Composable
fun DebugInfoRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ShapeOptionItem(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp, 60.dp) // Card size
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .then(
                    if (selected) Modifier.border(2.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon preview
            icon(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    // Get app version
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Description
                Text(
                    text = "Year Dots is a beautiful minimalist wallpaper that visualizes every day of the year as a dot. Watch your progress through the year on your lock screen!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // README
                AboutMenuItem(
                    icon = Icons.Default.Info,
                    title = "README",
                    subtitle = "Check the GitHub repository and the README",
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/ikrishnaa/YearDots")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No browser found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                // Latest Release
                AboutMenuItem(
                    icon = Icons.Default.Info,
                    title = "Latest release",
                    subtitle = "Look for changelogs and new versions",
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/ikrishnaa/YearDots/releases")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No browser found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                // GitHub Issues
                AboutMenuItem(
                    icon = Icons.Default.Info,
                    title = "GitHub issue",
                    subtitle = "Submit an issue for bug report or feature request",
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/ikrishnaa/YearDots/issues")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No browser found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                // Credits
                AboutMenuItem(
                    icon = Icons.Default.Info,
                    title = "Credits",
                    subtitle = "Built with Jetpack Compose",
                    onClick = { }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Version
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Version",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AboutMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
