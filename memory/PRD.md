# AuryxBrowser - Product Requirements Document

## Project Info
- **App Name:** AuryxBrowser
- **Version:** 1.305.02 (Updated from 1.305.01)
- **Author:** xDustAtom
- **Platform:** Native Android (Kotlin)
- **Build System:** GitHub Actions CI/CD

## Original Problem Statement
Create a complete Android application called "AuryxBrowser" - a fully functional modern web browser with:
- Clean interface, smooth performance
- Built-in tools (AuryxTools)
- Material Design 3 inspired dark theme
- Neon green accents

## Version 1.305.02 Updates

### New Features Implemented ✅
1. **Find in Page** - Real WebView text search
   - `webView.findAllAsync(query)` implementation
   - Match count display
   - Next/Previous navigation
   - Dialog-based UI matching dark theme

2. **Update Checker** - GitHub Pages version checking
   - Fetches from: https://aauroramarini05-hash.github.io/XDA.AuryxBrowser/
   - Parses APK filenames for version numbers
   - Shows update dialog with download option

3. **Page Info Tool** - Current page information display
   - Page URL
   - Page title
   - HTTPS status (secure/not secure)
   - Page load time
   - WebView user agent

4. **Auryx Assistant** - Local browser helper
   - Natural language command processing
   - Quick actions (open site, search, settings, etc.)
   - No API required - runs completely offline

5. **UI Improvements**
   - Consistent dark theme
   - Smooth animations
   - Green accent color maintained

## Files Modified
- `app/build.gradle.kts` - Version updated to 1.305.02
- `MainActivity.kt` - Added Find in Page, Page Info, Assistant action support
- `SettingsFragment.kt` - Added Update Checker
- `AuryxToolsFragment.kt` - Added Page Info and Assistant cards
- `fragment_settings.xml` - Added Check for Updates button
- `fragment_auryx_tools.xml` - Added new tool cards

## Files Created
- `PageInfoFragment.kt` - Page info display
- `AssistantFragment.kt` - Auryx Assistant UI
- `AuryxAssistant.kt` - Local command processing
- `UpdateChecker.kt` - Version checking utility
- `fragment_page_info.xml` - Page info layout
- `fragment_assistant.xml` - Assistant layout
- `dialog_find_in_page.xml` - Find in page dialog
- `ic_send.xml`, `ic_arrow_up.xml`, `ic_arrow_down.xml` - New icons
- `ic_page_info.xml`, `ic_assistant.xml`, `ic_update.xml` - Tool icons
- `bg_send_button.xml` - Send button background

## Technical Implementation
- **Find in Page**: Uses native WebView.findAllAsync() and WebView.FindListener
- **Update Checker**: OkHttp GET request + regex parsing for APK versions
- **Assistant**: Pattern matching for commands, no external API
- **Page Info**: Tracked via WebViewClient.onPageStarted/onPageFinished timestamps

## Project Statistics
- Total Kotlin files: 28
- Total Layout files: 19
- Total Drawable files: 30+
- Fully offline capable

## Next Steps / Backlog
- P1: Incognito/private browsing mode
- P2: Ad blocking
- P2: Reading mode
- P3: Custom homepage configuration
- P3: Gesture navigation
