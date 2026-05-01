---
sessionId: session-260430-170708-1ovc
isActive: false
---

# Requirements

### Overview & Goals
The goal is to bridge the remaining functionality and UX gaps between the legacy Android app (`app`) and the new Compose-based app (`app-compose`). 
Based on the analysis, several metadata fields (Tonality, Translator) are missing from the UI, and some system-level integrations (System theme, Intent filters) are not yet implemented in the new app.

### Scope
- **In Scope**:
    - Displaying and editing `Tonality` and `Translator` for songs.
    - Support for "System" (Auto) theme mode.
    - App version and License information in Settings.
    - Android Manifest intent parity (`VIEW`, `SENDTO`).
- **Out of Scope**:
    - Major visual redesigns.
    - Implementing full transposition logic (only displaying/editing the tonality field).

# Technical Design

### Current Implementation
- `SongDetailScreen` displays metadata like author, composer, year, and bible ref, but ignores `tonalities` and `translator`.
- `SongEditScreen` allows editing most fields but lacks `tonalities` and `translator`.
- `ThemeMode` only supports explicit `LIGHT`, `DARK`, and `BLACK`.
- `app-compose`'s `MainActivity` only handles the `MAIN` / `LAUNCHER` intent.

### Proposed Changes
1. **Metadata Parity**:
    - Update `SongDetailScreen` to show `tonalities` (as a comma-separated list of localized names) and `translator`.
    - Update `SongEditScreen` to include a text field for `translator` and a selection mechanism for `tonalities`.
2. **System Theme**:
    - Expand `ThemeMode` enum to include `SYSTEM`.
    - Modify `AppTheme` to detect system theme using `isSystemInDarkTheme()` when `SYSTEM` is active.
3. **About & License**:
    - Add app version display in `SettingsScreen`.
    - Add a "License" item in `SettingsScreen` that displays the PolyForm Noncommercial License 1.0.0 in a scrollable dialog.
4. **Manifest Intent Filters**:
    - Synchronize `MainActivity` intent filters with the legacy app to ensure the new app can handle similar system actions.

### Data Models / Contracts
- `SongEditUiState.Content`:
    - Add `translator: String`
    - Add `tonalities: List<Tonality>`
- `SongEditEvent`:
    - Add `TranslatorChanged(String)`
    - Add `TonalityToggled(Tonality)`

# Delivery Steps

### ✓ Step 1: Create Extended Parity Plan document
Define extra parity gaps and improvements.
- Create `docs/ai/plans/2026-05-01_extra-parity-and-polish.md`.
- List tasks for Tonality/Translator metadata and editing.
- List tasks for System theme support and License/About section.
- List tasks for Android Manifest intent parity.

### ✓ Step 2: Implement Tonality and Translator parity in Song screens
Add Tonality and Translator to Song Detail and Edit screens.
- Update `SongDetailScreen.kt`: display tonalities and translator in metadata.
- Update `SongEditUiState.kt` & `SongEditEvent.kt`: add fields for translator and tonalities.
- Update `SongEditScreenModel.kt`: load/save translator and tonalities.
- Update `SongEditScreen.kt`: add UI fields for translator and tonalities (text field and filter chips).

### ✓ Step 3: Add System theme support and About/License info
Add System theme support and License/About section.
- Update `ThemeMode.kt`: add `SYSTEM` mode.
- Update `Theme.kt`: use `isSystemInDarkTheme()` when `SYSTEM` is selected.
- Update `SettingsScreen.kt`: add "System" theme option, "About" version, and "License" dialog.
- Update `MainActivity.kt`: handle system theme and pass it to `AppRoot`.

### ✓ Step 4: Update Android Manifest for intent parity
Add missing intent filters to Android manifest.
- Update `pws-android/app-compose/src/main/AndroidManifest.xml`: add `VIEW` and `SENDTO` intents to `MainActivity`.