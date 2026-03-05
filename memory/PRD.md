# AuryxBrowser - Product Requirements Document

## Project Info
- **App Name:** AuryxBrowser
- **Version:** 1.305.02
- **Author:** xDustAtom
- **Platform:** Native Android (Kotlin)
- **Build System:** GitHub Actions CI/CD

## Language Support (NEW)
- English (default)
- Italian (Italiano)
- Spanish (Español)
- French (Français)
- German (Deutsch)

## Implementation Summary

### Language Switch Feature
**Files Created:**
- `LocaleHelper.kt` - Locale management utility
- `values/strings.xml` - English strings (180+ strings)
- `values-it/strings.xml` - Italian translations
- `values-es/strings.xml` - Spanish translations
- `values-fr/strings.xml` - French translations
- `values-de/strings.xml` - German translations

**Files Modified:**
- `AuryxApplication.kt` - Added `attachBaseContext` for locale
- `MainActivity.kt` - Added `attachBaseContext` for locale
- `SettingsFragment.kt` - Added language selection dialog
- `fragment_settings.xml` - Added language selection UI

### Technical Implementation
- Uses Android native `Locale` and `Configuration`
- Persists language choice via SharedPreferences
- Applies locale via `attachBaseContext` on Application and Activity
- Activity recreate() for immediate UI update

### Features (v1.305.02)
1. Find in Page - WebView text search
2. Update Checker - GitHub Pages version checking
3. Page Info Tool - Current page information
4. Auryx Assistant - Local browser helper
5. Language Switch - 5 languages supported

### Project Statistics
- Total Kotlin files: 29
- Total Layout files: 19
- Total Drawable files: 30+
- String resource files: 5 (one per language)
- Fully offline capable

### Next Steps / Backlog
- P1: Incognito/private browsing mode
- P2: Ad blocking
- P2: Reading mode
- P3: More languages (Portuguese, Russian, Japanese, etc.)
