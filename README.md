# Year Dots - 365 Progress Wallpaper

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/Min%20SDK-26-orange.svg" alt="Min SDK">
</p>

**Year Dots** is a minimalist Android wallpaper app that automatically updates your home screen daily with a visual 365-dot calendar representing your year's progress.

## ✨ Features

- 🎨 **365-Dot Calendar Grid** - Past days filled, today highlighted, future outlined
- 🔄 **Daily Auto-Update** - Wallpaper refreshes automatically at midnight (WorkManager)
- 🎨 **Fully Customizable** - Choose colors for past, today, future, and background
- 🔋 **Battery Friendly** - Efficient background tasks with minimal impact
- 📱 **100% Offline** - No internet required, no tracking, no ads
- 🌙 **AMOLED-Optimized** - Dark backgrounds save battery on modern displays

## 📱 Screenshots

*(Add screenshots here after building)*

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0 Oreo)
- Kotlin 1.9+

### Build & Install

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/year-dots.git
   cd year-dots
   ```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Run on emulator or physical device

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Background Tasks**: WorkManager
- **Data Persistence**: DataStore (Preferences)
- **Graphics**: Android Canvas API

## 📁 Project Structure

```
app/
├── core/              # WallpaperGenerator (Canvas drawing logic)
├── data/              # SettingsRepository (DataStore)
├── worker/            # WallpaperWorker (background updates)
├── util/              # WorkScheduler
├── receiver/          # BootReceiver (reschedule after reboot)
└── ui/theme/          # Material 3 theme
```

## 🎯 How It Works

1. **Initialize**: App sets initial wallpaper and schedules daily WorkManager task
2. **Daily Update**: At ~00:01, WorkManager triggers `WallpaperWorker`
3. **Generate**: Worker fetches settings, calculates current day, draws 365 dots via Canvas
4. **Apply**: Uses `WallpaperManager` to set new bitmap as wallpaper
5. **Persist**: Settings stored in DataStore, work rescheduled after device reboot

## 🧪 Testing

See [walkthrough.md](walkthrough.md) for detailed testing instructions including:
- Manual testing steps
- ADB commands for date simulation
- WorkManager verification

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**Krishana**  
Year Dots v1.0 - January 2026

## 🙏 Acknowledgments

Inspired by:
- "4,000 Weeks" philosophy
- Memento mori tradition
- Life calendar visualizations (WeeklyDots, One Dot, etc.)

---

> "Time is passing. Make it count." ⏳
