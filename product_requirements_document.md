# Product Requirements Document (PRD)

## App Overview
**App Name (Working Title):** Year Dots – 365 Progress Wallpaper  
**Version:** 1.0 MVP  
**Date:** January 29, 2026  
**Author:** Krishana  
**Target Platform:** Native Android  
**Development Approach:** Kotlin (Jetpack Compose)  
**Business Goal:** Personal experiment / potential free Play Store release.

## 1. Problem & Opportunity
**Context:**  
People increasingly seek subtle, visual reminders of time passing to stay mindful and productive (inspired by "memento mori", "4,000 Weeks" philosophy).

**Current Solutions & Gaps:**  
- Existing solutions are mostly iOS Shortcuts or manual web generators.
- Android alternatives are often manual, web-dependent, or lack true offline auto-daily wallpaper updates.

**Core Value Proposition:**  
A fully offline Android app where every day at ~midnight, the phone's wallpaper automatically updates to show one more filled dot. A quiet, always-visible reminder: "Time is passing. Make it count."

## 2. Target User
- **Demographics:** Age 18–45. India-focused initially (IST timezone).
- **Psychographics:** Productivity enthusiasts, minimalists, self-improvement focused.
- **Behavior:** Set once → forget → glance at wallpaper multiple times/day.

## 3. Key Goals & Success Metrics (MVP)
**Success Criteria:**  
- App installs and sets the initial wallpaper successfully.
- Background task runs daily to update the wallpaper (Doze mode delays acceptable).
- Customizations (colors/start date) are reflected correctly.
- No crashes on Android 12–15.
- Minimal battery impact.

**Metrics:**  
- First launch success rate > 90%
- Daily background update success rate > 80%
- Crash rate < 1%

## 4. Features – Prioritized

### MVP (Must-Have)
1.  **Core Wallpaper Generation**
    - **Visual:** 365 dots (Jan 1 – Dec 31). 7-column grid (weeks).
    - **States:**
        - Past days: Filled solid color (default: Blue[700])
        - Today: Highlighted (default: OrangeAccent + White border)
        - Future days: Empty/Outline (default: Grey[800])
    - **Background:** Solid dark (Black) for AMOLED/battery efficiency.
    - **Resolution:** Device-appropriate density/resolution.

2.  **Offline Date/Time Detection & Auto-Update**
    - **Source:** Local device clock (`java.time.LocalDate` / `Calendar`).
    - **Trigger:** Android Jetpack `WorkManager` (PeriodicWorkRequest).
    - **Logic:** Check current day vs. last saved update. Regenerate if changed.
    - **Constraints:** 100% offline.

3.  **Wallpaper Setting**
    - **Mechanism:** Generate Bitmap via `android.graphics.Canvas`.
    - **Apply:** Use `android.app.WallpaperManager` to write Bitmap stream to Home/Lock screen.

4.  **Basic Settings Screen**
    - **UI Style:** Material 3 (Jetpack Compose).
    - **Customizable Options:**
        - Past days color
        - Today highlight color
        - Future/Empty color
        - Background color
    - **Actions:** "Apply Changes Now" button.

5.  **Permissions & Setup Flow**
    - Request `SET_WALLPAPER`.
    - Handle Scoped Storage if saving files (though direct stream to WallpaperManager preferred).
    - First launch onboarding: Generate -> Set -> Schedule Work -> Success Toast.

### Nice-to-Have (Post-MVP)
- Custom start date (Birthday mode).
- Overlay text ("% year complete").
- Grid variations (Square, Circular).

## 5. Non-Functional Requirements
- **Offline-First:** No API calls, no analytics (privacy compliant).
- **Battery:** Efficient `WorkManager` (Constraints: BatteryNotLow).
- **Performance:** Generation < 1s (Native Canvas is very fast).
- **Compatibility:** Android 8.0+ (API 26).

## 6. Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Key Libraries:**
    - `androidx.work:work-runtime-ktx` (Background scheduling)
    - `androidx.datastore` (Settings persistence) or `SharedPreferences`
    - `android.graphics.*` (Bitmap & Canvas for drawing)
    - `java.time.*` (Date handling)
- **Deployment:** Android APK / Play Store.

## 7. Risks & Mitigations
- **Doze Mode:** May delay midnight update by 30-60 mins. *Mitigation: Accepted behavior; use `FlexInterval` in WorkManager.*
- **Permissions:** Android restrictions. *Mitigation: Clear in-app guide and retry UI.*
- **Manufacturer Restrictions:** Some OEMs kill background workers (e.g., Xiaomi/OnePlus). *Mitigation: "Don't kill my app" guide if needed.*
