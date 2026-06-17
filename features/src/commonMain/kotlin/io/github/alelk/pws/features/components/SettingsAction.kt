package io.github.alelk.pws.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.settings_open
import org.jetbrains.compose.resources.stringResource

/**
 * Top-bar action that opens the Settings screen. Use inside `actions = { ... }`
 * to avoid copy-pasting the same IconButton across every primary screen.
 */
@Composable
fun SettingsAction() {
  val navigator = LocalNavigator.currentOrThrow
  IconButton(
    onClick = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) },
    modifier = Modifier.testTag("action:open-settings"),
  ) {
    Icon(
      imageVector = Icons.Filled.Settings,
      contentDescription = stringResource(Res.string.settings_open),
    )
  }
}
