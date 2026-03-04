# AuryxBrowser

**Version:** 1.305.01  
**Author:** xDustAtom

A fast, lightweight, and feature-rich Android web browser with a modern dark theme and neon green accents.

![AuryxBrowser](app/src/main/res/drawable/ic_logo.xml)

## Features

### Core Browser
- 🌐 Full HTML5 support via Android WebView
- 🔒 HTTPS support
- 🔍 Integrated search and URL bar
- 🦆 DuckDuckGo as default search engine
- 📱 Desktop mode toggle
- ⬅️➡️ Back/Forward navigation

### Tab Management
- 📋 Unlimited tabs
- 🔀 Easy tab switching with overview screen
- ➕ Quick new tab creation
- 👆 Long-press links to open in new tab

### Bookmarks & History
- ⭐ Add pages to bookmarks
- 📚 Bookmark management
- 📅 Full browsing history
- 🗑️ Clear history option

### Download Manager
- 📥 Automatic download detection
- 📊 Download progress tracking
- 📂 Open downloaded files

### AuryxTools (Built-in Utilities)

#### Network Monitor
- 🌐 Public IP address display
- 📶 Connection type (WiFi/4G/5G)
- ⏱️ Network latency measurement
- 🟢 Real-time network state

#### Device Information
- 📱 Phone model & manufacturer
- 💻 CPU architecture
- 💾 RAM information
- 🤖 Android version
- 🔋 Battery level

#### Performance Monitor
- 📊 Real-time RAM usage
- ⚙️ CPU usage tracking
- 📈 Live updating statistics

### Settings
- ⚙️ JavaScript toggle
- 🚫 Pop-up blocking
- 🖱️ Desktop mode
- 🔍 Search engine selection (DuckDuckGo, Google, Bing, Yahoo)
- 🗑️ Cache & history clearing

## Design

- 🌙 Modern dark theme
- 🟢 Neon green accents (#39FF14)
- 🎨 Material Design 3 inspired
- ✨ Smooth animations
- 🟣 Rounded UI elements

## Requirements

- Android 6.0 (API 23) or higher
- Minimal RAM usage
- Optimized for low-end devices

## Building

### Using GitHub Actions (Recommended)

1. Push code to GitHub repository
2. GitHub Actions will automatically build the APK
3. Download artifacts from the Actions tab

### Local Build

```bash
# Clone repository
git clone https://github.com/xDustAtom/AuryxBrowser.git
cd AuryxBrowser

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### Build Outputs

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Project Structure

```
AuryxBrowser/
├── app/
│   ├── src/main/
│   │   ├── java/com/xdustatom/auryxbrowser/
│   │   │   ├── activities/      # Main activity
│   │   │   ├── adapters/        # RecyclerView adapters
│   │   │   ├── fragments/       # UI fragments
│   │   │   ├── models/          # Data models
│   │   │   ├── utils/           # Utility classes
│   │   │   └── services/        # Background services
│   │   └── res/
│   │       ├── layout/          # XML layouts
│   │       ├── drawable/        # Icons and graphics
│   │       ├── values/          # Colors, strings, themes
│   │       └── menu/            # Navigation menus
│   └── build.gradle.kts
├── .github/workflows/
│   └── build.yml            # GitHub Actions workflow
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## License

MIT License - Feel free to use and modify.

## Author

Created by **xDustAtom**

---

*AuryxBrowser - Fast. Private. Powerful.*
