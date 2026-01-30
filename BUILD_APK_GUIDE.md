# Building Year Dots APK - Testing Guide

## Method 1: Using Android Studio (Recommended)

### Step 1: Open Project
1. Open Android Studio
2. Click **File → Open**
3. Navigate to `/home/ikrishnaa/Desktop/Onedot`
4. Click **OK**

### Step 2: Let Gradle Sync
- Android Studio will automatically sync Gradle dependencies
- Wait for "Gradle sync finished" message (bottom status bar)
- If you see any errors, click "Sync Now"

### Step 3: Build Debug APK
1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete (~1-3 minutes)
3. You'll see a notification: "APK(s) generated successfully"
4. Click **locate** in the notification

**APK Location:**
```
/home/ikrishnaa/Desktop/Onedot/app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Transfer to Phone
Choose one method:

**A. USB Cable:**
```bash
# Enable USB Debugging on phone first (Developer Options → USB Debugging)
adb install /home/ikrishnaa/Desktop/Onedot/app/build/outputs/apk/debug/app-debug.apk
```

**B. Google Drive/Dropbox:**
1. Upload `app-debug.apk` to cloud storage
2. Open link on phone
3. Download and install

**C. USB File Transfer:**
1. Connect phone via USB
2. Copy `app-debug.apk` to phone's Downloads folder
3. Open "Files" app on phone
4. Tap the APK file
5. Allow "Install from Unknown Sources" if prompted
6. Click **Install**

---

## Method 2: Using Command Line (Without Android Studio)

### Prerequisites
Install Gradle:
```bash
sudo snap install gradle --classic
# OR
sudo apt install gradle
```

### Build Debug APK
```bash
cd /home/ikrishnaa/Desktop/Onedot
gradle wrapper  # First time only
./gradlew assembleDebug
```

**Output:**
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Method 3: Install Gradle & Build Now

If you want me to automate the build process, run these commands in your terminal:

```bash
# Install Gradle via snap (recommended)
sudo snap install gradle --classic

# Navigate to project
cd /home/ikrishnaa/Desktop/Onedot

# Create gradle wrapper
gradle wrapper

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## Installing on Your Phone

### Enable Installation from Unknown Sources

**Android 8.0 - 12:**
1. Go to **Settings → Apps → Special app access → Install unknown apps**
2. Select your file manager (e.g., Files, Chrome)
3. Enable **Allow from this source**

**Android 13+:**
1. Go to **Settings → Security → Install unknown apps**
2. Select file source
3. Enable installation

### Install the APK
1. Transfer `app-debug.apk` to your phone
2. Open the file (using Files app or Downloads)
3. Tap **Install**
4. Grant **SET_WALLPAPER** permission when prompted
5. Open the app
6. Customize colors (optional)
7. Tap **Apply Now**
8. Check your home screen - you should see the 365-dot wallpaper!

---

## Testing the App

1. **Initial Setup:**
   - Open app → Tap "Apply Now"
   - Go to home screen → Verify wallpaper shows dots

2. **Color Customization:**
   - Change any color → Tap "Apply Now"
   - Verify wallpaper updates

3. **Daily Update (Next Day):**
   - Wait until tomorrow
   - Check wallpaper - one more dot should be filled

4. **After Reboot:**
   - Restart phone
   - Wallpaper should persist
   - Daily updates should continue

---

## Troubleshooting

**"App not installed" error:**
- Uninstall any previous version
- Check Android version (requires Android 8.0+)

**Wallpaper not setting:**
- Grant wallpaper permission in Settings → Apps → Year Dots → Permissions

**Daily updates not working:**
- Check battery optimization: Settings → Apps → Year Dots → Battery → Unrestricted

---

## Questions?

If you encounter any issues during the build or installation process, let me know!
