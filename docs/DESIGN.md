# Design system — "Songs of Revival" redesign (2026-07)

Visual language for all Compose Multiplatform UI in `:features`. Implemented in
`features/src/commonMain/kotlin/io/github/alelk/pws/features/theme/` (`Color.kt`,
`Typography.kt`) and the components referenced below. When adding or restyling a
screen, follow these tokens instead of inventing new colors or type styles.

## Principles

- Quiet, focused, timeless — like a well-printed songbook.
- **One accent** (conifer green). Warm neutrals instead of pure white/grey.
- **Reading and search come first.** Less chrome, fewer choices.
- One visual language for a song row everywhere: `[number badge] Title + Book [+ snippet]`.

## Color

Tokens live in `theme/Color.kt` and are mapped onto the Material 3 `ColorScheme`
(`LightColors`, `DarkColors`, `BlackColors`).

**Light theme (warm)**

| Token            | Value     | Role                                          |
|------------------|-----------|-----------------------------------------------|
| accent / conifer | `#2E5A4D` | `primary`; the only accent                    |
| accent-soft      | `#E7EFEA` | `primaryContainer`; number badges, soft fills |
| app background   | `#F1EBDF` | `background`                                  |
| reading paper    | `#F5EFE3` | `surfaceContainer`                            |
| card             | `#FBF7EE` | `surface`, `surfaceContainerLow`              |
| input field      | `#FFFDF8` | `surfaceContainerLowest`                      |
| ink              | `#22201A` | `onSurface` / `onBackground`                  |
| ink-2            | `#6B6353` | `onSurfaceVariant` (secondary text)           |
| ink-3 (quiet)    | `#8A8172` | `outline` (quiet text)                        |
| line             | `#E3DACA` | `outlineVariant` (dividers)                   |

**"Warm Night" dark theme** (replaces the old cold `#121212` Dark)

background `#17150F` · ink `#ECE6D6` · ink-2 `#928B79` · line `#2C2A20` ·
accent = sage `#8FBFA4`.

**OLED black**

background `#000000` · surface `#0B0B0B` · ink `#E9E4D7` · line `#1C1C18` ·
accent = sage `#8FBFA4`. True black is kept even when dynamic color is enabled
(battery on AMOLED wins).

**Category/tag tints** (muted, roughly equal saturation; `CategoryTints` list):
conifer `#2E5A4D` · clay `#9E5A3C` · slate `#435E77` · plum `#6B4860` · gold `#A98526`.

**Destructive** (remove/delete): `#B5502E` (light) / `#E08963` (dark themes).

**Search-match highlight:** background `#2E5A4D` at 14% alpha (`0x24`), text `#254E43`,
radius 4.

## Typography

- **UI:** system grotesque (SF on iOS). Headings weight 600–700.
- **Song text:** grotesque by default; the "Serif font" reading option switches the
  song title and lyrics to a literary serif (`SongSerifFontFamily`). Minimum song
  text size 17sp (`SongTextMinSize`); the "Aa" sheet controls size and line height.
- **Numbers / small technical labels:** monospace (`NumberBadgeTextStyle`).

## Components

- **Number badge:** mono 600, conifer on accent-soft, radius 8 (`NumberBadge`).
- **Chorus/bridge:** thin 2dp accent line on the left + uppercase mono label —
  **no filled tile** (`SongDetailScreen.IntrinsicChorusView`).
- **Book cover:** warm paper + flat category-tinted header with a translucent spine
  strip, serif title on the tint, song count on the paper footer. The tint is a
  stable hash of the book id into `CategoryTints` — never a random gradient
  (`BookCard.bookTint`).
- **Bottom nav:** 4 line icons — Home · Books · Search · Library (`NavDestination`).
- **Segmented switcher** (Library): pill container on a warm tint, selected segment
  filled with the conifer accent (`LibraryScreen.LibrarySegmentedControl`).
- **Search field:** the mono "123 / abc" pill toggles the numeric keyboard; numeric
  queries search by song number (`SearchField`, `SearchBarWithSuggestions`).
- **Tag:** colored indicator; the *shape* is derived from the color hash
  (circle/star/square/diamond) for color-blind distinguishability — no shape column
  in the DB.

## Screen decisions

- **Song (frames 1A/2A/3A):** favorite heart in the top bar; prev/next arrows are
  adaptive (swipe on touch, buttons on web/desktop) and can be hidden in the "Aa"
  sheet; the "Aa" sheet controls size, line height, serif option.
- **Dark (3A/3B):** "Warm Night" replaces the cold Dark; OLED = existing Black;
  accent lightens to sage.
- **Home (5A):** search hero first; songbook shelf second (popularity sorting —
  see the deferred plan below); recently opened at the bottom as compact rows.
- **Search (6A/6B/7):** live results as you type, no Enter jump (one surface,
  Cancel returns); filter All / In books / Standalone; number path = numeric
  keyboard + book context.
- **Library (8):** Favorites + History + Tags under the segment switcher; song rows
  use the shared row language; history grouped by day.

## Deferred (intentionally not implemented)

- **Sort songbooks by popularity** — no schema change needed
  (`BookStatistic.readings` / `HistoryEntry.viewCount` already exist); see
  [
  `ai/plans/2026-07-03_book-popularity-sorting_plan.md`](ai/plans/2026-07-03_book-popularity-sorting_plan.md).
- **Stored tag shape** (manual user choice) would need a migration — the derived
  `hash % 4` shape stays as is.
- Persisting the new `SongDetailDisplaySettings.serifFont` /
  `showNavigationButtons` fields is host-side work (`pws-android`).

Last reviewed: 2026-07-03
