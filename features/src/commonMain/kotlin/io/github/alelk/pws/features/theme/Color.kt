package io.github.alelk.pws.features.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Design system tokens (docs/DESIGN.md).
// Single conifer accent; warm neutrals instead of pure white/grey.

// --- Accent ---
val AccentConifer = Color(0xFF2E5A4D)      // conifer — the only accent of the light theme
val AccentConiferDeep = Color(0xFF254E43)  // text on the search-match highlight
val AccentSoft = Color(0xFFE7EFEA)         // accent-soft background (containers, number badge)
val AccentSage = Color(0xFF8FBFA4)         // sage — accent of the dark themes

// --- Light theme: warm neutrals ---
val WarmBackground = Color(0xFFF1EBDF)     // app background
val WarmPaper = Color(0xFFF5EFE3)          // reading paper
val WarmCard = Color(0xFFFBF7EE)           // card
val WarmField = Color(0xFFFFFDF8)          // input field
val WarmInk = Color(0xFF22201A)            // primary text
val WarmInk2 = Color(0xFF6B6353)           // secondary text
val WarmInk3 = Color(0xFF8A8172)           // quiet text
val WarmLine = Color(0xFFE3DACA)           // dividers

// --- "Warm Night" dark theme ---
val NightBackground = Color(0xFF17150F)
val NightInk = Color(0xFFECE6D6)
val NightInk2 = Color(0xFF928B79)
val NightLine = Color(0xFF2C2A20)

// --- OLED black theme ---
val OledBackground = Color(0xFF000000)
val OledSurface = Color(0xFF0B0B0B)
val OledInk = Color(0xFFE9E4D7)
val OledLine = Color(0xFF1C1C18)

// --- Category/tag tints (muted, roughly equal saturation) ---
val TintConifer = Color(0xFF2E5A4D)
val TintClay = Color(0xFF9E5A3C)
val TintSlate = Color(0xFF435E77)
val TintPlum = Color(0xFF6B4860)
val TintGold = Color(0xFFA98526)

val CategoryTints = listOf(TintConifer, TintClay, TintSlate, TintPlum, TintGold)

// --- Destructive (remove/delete) ---
val Destructive = Color(0xFFB5502E)
val DestructiveDark = Color(0xFFE08963)

val LightColors = lightColorScheme(
  primary = AccentConifer,
  onPrimary = Color(0xFFFFFDF8),
  primaryContainer = AccentSoft,
  onPrimaryContainer = Color(0xFF1D3B32),
  secondary = WarmInk2,
  onSecondary = Color(0xFFFFFDF8),
  secondaryContainer = Color(0xFFEFE7D7),
  onSecondaryContainer = Color(0xFF3E3729),
  tertiary = AccentConiferDeep,
  onTertiary = Color(0xFFFFFDF8),
  tertiaryContainer = AccentSoft,
  onTertiaryContainer = Color(0xFF1D3B32),
  error = Destructive,
  onError = Color(0xFFFFFDF8),
  errorContainer = Color(0xFFF7E0D6),
  onErrorContainer = Color(0xFF6E2E17),
  outline = WarmInk3,
  background = WarmBackground,
  onBackground = WarmInk,
  surface = WarmCard,
  onSurface = WarmInk,
  surfaceVariant = Color(0xFFEFE7D7),
  onSurfaceVariant = WarmInk2,
  inverseSurface = Color(0xFF333026),
  inverseOnSurface = WarmPaper,
  inversePrimary = AccentSage,
  surfaceTint = AccentConifer,
  outlineVariant = WarmLine,
  scrim = Color(0xFF000000),
  surfaceContainerLowest = WarmField,
  surfaceContainerLow = WarmCard,
  surfaceContainer = WarmPaper,
  surfaceContainerHigh = Color(0xFFEFE7D7),
  surfaceContainerHighest = Color(0xFFE9DFCC),
)

// "Warm Night" — replaces the cold Dark #121212
val DarkColors = darkColorScheme(
  primary = AccentSage,
  onPrimary = Color(0xFF12291F),
  primaryContainer = Color(0xFF24463A),
  onPrimaryContainer = Color(0xFFD6E9DC),
  secondary = Color(0xFFADA48F),
  onSecondary = Color(0xFF262217),
  secondaryContainer = Color(0xFF383325),
  onSecondaryContainer = Color(0xFFE5DFCE),
  tertiary = Color(0xFF79AC92),
  onTertiary = Color(0xFF0F241B),
  tertiaryContainer = Color(0xFF1F3A2F),
  onTertiaryContainer = Color(0xFFD6E9DC),
  error = DestructiveDark,
  onError = Color(0xFF3A1505),
  errorContainer = Color(0xFF6E2E17),
  onErrorContainer = Color(0xFFF7DED2),
  outline = NightInk2,
  background = NightBackground,
  onBackground = NightInk,
  surface = Color(0xFF1C1914),
  onSurface = NightInk,
  surfaceVariant = NightLine,
  onSurfaceVariant = Color(0xFFB0A890),
  inverseSurface = NightInk,
  inverseOnSurface = Color(0xFF2A261D),
  inversePrimary = AccentConifer,
  surfaceTint = AccentSage,
  outlineVariant = NightLine,
  scrim = Color(0xFF000000),
  surfaceContainerLowest = Color(0xFF12100B),
  surfaceContainerLow = Color(0xFF1C1914),
  surfaceContainer = Color(0xFF211E16),
  surfaceContainerHigh = Color(0xFF272319),
  surfaceContainerHighest = NightLine,
)

// OLED black: true black background, warm ink, sage accent
val BlackColors = darkColorScheme(
  primary = AccentSage,
  onPrimary = Color(0xFF12291F),
  primaryContainer = Color(0xFF1E3B30),
  onPrimaryContainer = Color(0xFFD6E9DC),
  secondary = Color(0xFFA59D89),
  onSecondary = Color(0xFF23201A),
  secondaryContainer = Color(0xFF2A281F),
  onSecondaryContainer = Color(0xFFDFD9C9),
  tertiary = Color(0xFF79AC92),
  onTertiary = Color(0xFF0F241B),
  tertiaryContainer = Color(0xFF1A322A),
  onTertiaryContainer = Color(0xFFD6E9DC),
  error = DestructiveDark,
  onError = Color(0xFF3A1505),
  errorContainer = Color(0xFF6E2E17),
  onErrorContainer = Color(0xFFF7DED2),
  outline = Color(0xFF8A8272),
  background = OledBackground,
  onBackground = OledInk,
  surface = OledBackground,
  onSurface = OledInk,
  surfaceVariant = Color(0xFF14130E),
  onSurfaceVariant = Color(0xFFA59D89),
  inverseSurface = OledInk,
  inverseOnSurface = Color(0xFF1C1C18),
  inversePrimary = AccentConifer,
  surfaceTint = AccentSage,
  scrim = Color(0xFF000000),
  surfaceContainerLowest = OledBackground,
  surfaceContainerLow = OledSurface,
  surfaceContainer = Color(0xFF11110D),
  surfaceContainerHigh = Color(0xFF171712),
  surfaceContainerHighest = OledLine,
)
