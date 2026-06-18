package io.github.alelk.pws.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.features.components.AppTopBar
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.settings_books
import io.github.alelk.pws.features.resources.settings_books_subtitle
import io.github.alelk.pws.features.resources.settings_about
import io.github.alelk.pws.features.resources.settings_donation_title
import io.github.alelk.pws.features.resources.settings_donation_subtitle
import io.github.alelk.pws.features.resources.settings_version
import io.github.alelk.pws.features.resources.settings_license
import io.github.alelk.pws.features.resources.settings_developers
import io.github.alelk.pws.features.resources.settings_export
import io.github.alelk.pws.features.resources.settings_import
import io.github.alelk.pws.features.resources.settings_import_export
import io.github.alelk.pws.features.resources.settings_import_export_subtitle
import io.github.alelk.pws.features.resources.settings_interface
import io.github.alelk.pws.features.resources.settings_theme_subtitle
import io.github.alelk.pws.features.resources.settings_title
import io.github.alelk.pws.features.resources.common_close
import io.github.alelk.pws.features.resources.license_load_failed
import io.github.alelk.pws.features.resources.license_loading
import io.github.alelk.pws.features.theme.LocalThemeSettings
import io.github.alelk.pws.features.theme.ThemeMode
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource

class SettingsScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SettingsScreenModel>()
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val themeSettings = LocalThemeSettings.current
    val externalActions = LocalSettingsExternalActions.current

    LaunchedEffect(themeSettings?.themeMode) {
      themeSettings?.let { viewModel.onEvent(SettingsEvent.SyncTheme(it.themeMode)) }
    }

    LaunchedEffect(Unit) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is SettingsScreenModel.Effect.ApplyTheme -> {
            themeSettings?.onThemeModeChange?.invoke(effect.themeMode)
          }

          is SettingsScreenModel.Effect.OpenUrl -> {
            externalActions?.openUrl?.invoke(effect.url)
          }

          is SettingsScreenModel.Effect.SendEmail -> {
            externalActions?.sendEmail?.invoke(effect.mailto)
          }

          SettingsScreenModel.Effect.ExportData -> {
            externalActions?.exportBackup?.invoke()
          }

          SettingsScreenModel.Effect.ImportData -> {
            externalActions?.importBackup?.invoke()
          }

          SettingsScreenModel.Effect.NavigateBack -> {
            navigator.pop()
          }
        }
      }
    }

    SettingsContent(
      state = state,
      onBack = { viewModel.onEvent(SettingsEvent.Back) },
      onThemeSelected = { mode -> viewModel.onEvent(SettingsEvent.SetTheme(mode)) },
      onBookToggle = { id, enabled -> viewModel.onEvent(SettingsEvent.ToggleBook(id, enabled)) },
      onDeveloperClick = { contact -> viewModel.onEvent(SettingsEvent.OpenDeveloperContact(contact)) },
      onExportClick = { viewModel.onEvent(SettingsEvent.ExportData) },
      onImportClick = { viewModel.onEvent(SettingsEvent.ImportData) },
      onDonationClick = { viewModel.onEvent(SettingsEvent.OpenDonation) },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
  state: SettingsUiState,
  onBack: () -> Unit,
  onThemeSelected: (ThemeMode) -> Unit,
  onBookToggle: (io.github.alelk.pws.domain.core.ids.BookId, Boolean) -> Unit,
  onDeveloperClick: (DeveloperContact) -> Unit,
  onExportClick: () -> Unit,
  onImportClick: () -> Unit,
  onDonationClick: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  var showLicenseDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      AppTopBar(
        title = stringResource(Res.string.settings_title),
        canNavigateBack = true,
        onNavigateBack = onBack
      )
    }
  ) { innerPadding ->
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val current = state) {
        SettingsUiState.Loading -> Unit
        is SettingsUiState.Content -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            contentPadding = PaddingValues(MaterialTheme.spacing.screenHorizontal)
          ) {
            item {
              Text(
                text = stringResource(Res.string.settings_interface),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
              )
            }
            item {
              SettingsSectionCard(footer = stringResource(Res.string.settings_theme_subtitle)) {
                current.themes.forEachIndexed { index, item ->
                  ThemeRow(
                    title = stringResource(item.titleRes),
                    icon = themeModeIcon(item.themeMode),
                    isSelected = item.themeMode == current.selectedTheme,
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onThemeSelected(item.themeMode)
                    }
                  )
                  if (index < current.themes.lastIndex) {
                    HorizontalDivider(
                      modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                  }
                }
              }
            }

            item {
              SectionTitle(stringResource(Res.string.settings_developers))
            }

            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
              ) {
                Column {
                  current.developers.forEachIndexed { index, dev ->
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          onDeveloperClick(dev.contact) 
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = stringResource(dev.nameRes),
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = stringResource(dev.roleRes),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                    if (index < current.developers.lastIndex) {
                      HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                      )
                    }
                  }
                }
              }
            }

            item {
              SectionTitle(stringResource(Res.string.settings_books))
            }

            item {
              SettingsSectionCard(footer = stringResource(Res.string.settings_books_subtitle)) {
                current.books.forEachIndexed { index, book ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = book.title,
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f)
                    )
                    Switch(
                      checked = book.enabled,
                      onCheckedChange = { checked ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBookToggle(book.id, checked)
                      }
                    )
                  }
                  if (index < current.books.lastIndex) {
                    HorizontalDivider(
                      modifier = Modifier.padding(horizontal = 16.dp),
                      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                  }
                }
              }
            }

            item {
              SectionTitle(stringResource(Res.string.settings_import_export))
            }

            item {
              SettingsSectionCard(footer = stringResource(Res.string.settings_import_export_subtitle)) {
                Column(modifier = Modifier.padding(16.dp)) {
                  OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onExportClick()
                    }
                  ) {
                    Text(stringResource(Res.string.settings_export))
                  }
                  Spacer(Modifier.height(8.dp))
                  OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onImportClick()
                    }
                  ) {
                    Text(stringResource(Res.string.settings_import))
                  }
                }
              }
            }

            // Donation section — visible only for loyal users
            if (current.showDonationSection) {
              item {
                SectionTitle(stringResource(Res.string.settings_donation_title))
              }
              item {
                DonationCard(
                  onDonationClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDonationClick()
                  }
                )
              }
            }

            item {
              SectionTitle(stringResource(Res.string.settings_about))
            }

            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
              ) {
                Column {
                  // Version
                  if (state is SettingsUiState.Content && state.appVersion.isNotBlank()) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = stringResource(Res.string.settings_version, state.appVersion),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                    HorizontalDivider(
                      modifier = Modifier.padding(horizontal = 16.dp),
                      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                  }

                  // License
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showLicenseDialog = true
                      }
                      .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = stringResource(Res.string.settings_license),
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showLicenseDialog) {
    LicenseDialog(onDismiss = { showLicenseDialog = false })
  }
}

@Composable
private fun LicenseDialog(onDismiss: () -> Unit) {
  var licenseText by remember { mutableStateOf<String?>(null) }
  val loadingText = stringResource(Res.string.license_loading)
  val loadFailedFmt = stringResource(Res.string.license_load_failed, "%s")
  LaunchedEffect(Unit) {
    runCatching {
      io.github.alelk.pws.features.resources.Res.readBytes("files/LICENSE.txt").decodeToString()
    }.onSuccess { licenseText = it }
      .onFailure { licenseText = loadFailedFmt.replace("%s", it.message ?: "") }
  }

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.settings_license)) },
    text = {
      Box(modifier = Modifier.height(400.dp).verticalScroll(rememberScrollState())) {
        Text(
          text = licenseText ?: loadingText,
          style = MaterialTheme.typography.bodySmall
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.common_close))
      }
    }
  )
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.semantics { heading() }
  )
}

@Composable
private fun DonationCard(onDonationClick: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onDonationClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(Res.string.settings_donation_title),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
          text = stringResource(Res.string.settings_donation_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
      }
    }
  }
}

@Composable
private fun ThemeRow(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(end = 16.dp)
    )
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier
        .weight(1f)
        .padding(vertical = 8.dp)
    )
    RadioButton(
      selected = isSelected,
      onClick = onClick
    )
  }
}

/** Иконка для каждого варианта темы — iOS-style leading icon. */
private fun themeModeIcon(mode: io.github.alelk.pws.features.theme.ThemeMode): ImageVector = when (mode) {
  io.github.alelk.pws.features.theme.ThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
  io.github.alelk.pws.features.theme.ThemeMode.LIGHT -> Icons.Filled.LightMode
  io.github.alelk.pws.features.theme.ThemeMode.DARK -> Icons.Filled.DarkMode
  io.github.alelk.pws.features.theme.ThemeMode.BLACK -> Icons.Filled.Contrast
}

/**
 * iOS-style секция настроек: карточка + опциональный footer-text под ней.
 * Пояснительный текст идёт под карточкой серым меньшим шрифтом — как в Settings.app.
 */
@Composable
private fun SettingsSectionCard(
  footer: String? = null,
  content: @Composable () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        content()
      }
    }
    if (footer != null) {
      Text(
        text = footer,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )
    }
  }
}
