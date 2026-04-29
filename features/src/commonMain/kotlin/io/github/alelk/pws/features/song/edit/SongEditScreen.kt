package io.github.alelk.pws.features.song.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_close
import io.github.alelk.pws.features.resources.common_error_title
import io.github.alelk.pws.features.resources.song_edit_discard_cancel
import io.github.alelk.pws.features.resources.song_edit_discard_confirm
import io.github.alelk.pws.features.resources.song_edit_discard_message
import io.github.alelk.pws.features.resources.song_edit_discard_title
import io.github.alelk.pws.features.resources.song_edit_error_not_found
import io.github.alelk.pws.features.resources.song_edit_label_number
import io.github.alelk.pws.features.resources.song_edit_label_tags
import io.github.alelk.pws.features.resources.song_edit_label_text
import io.github.alelk.pws.features.resources.song_edit_label_title
import io.github.alelk.pws.features.resources.song_edit_loading
import io.github.alelk.pws.features.resources.song_edit_save_error_prefix
import io.github.alelk.pws.features.resources.song_edit_save_success
import io.github.alelk.pws.features.resources.song_edit_title
import io.github.alelk.pws.features.resources.song_edit_validation_text_required
import io.github.alelk.pws.features.resources.song_edit_validation_title_required
import io.github.alelk.pws.features.resources.tags_save
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class SongEditScreen(val songId: SongId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongEditScreenModel>(parameters = { parametersOf(songId) })
    val state by viewModel.state.collectAsState()
    val showDiscardDialog by viewModel.showDiscardDialog.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is SongEditScreenModel.Effect.NavigateBack -> navigator.pop()
          is SongEditScreenModel.Effect.ShowSnackbar -> {
            val message = when (effect.message) {
              SongEditSnackbarMessage.Saved -> getString(Res.string.song_edit_save_success)
            }
            scope.launch { snackbarHostState.showSnackbar(message) }
          }
        }
      }
    }

    SongEditContent(
      state = state,
      showDiscardDialog = showDiscardDialog,
      snackbarHostState = snackbarHostState,
      onEvent = viewModel::onEvent,
      onNavigateBack = { navigator.pop() }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongEditContent(
  state: SongEditUiState,
  showDiscardDialog: Boolean,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  onEvent: (SongEditEvent) -> Unit,
  onNavigateBack: () -> Unit
) {
  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = { Text(stringResource(Res.string.song_edit_title)) },
        navigationIcon = {
          IconButton(onClick = { onEvent(SongEditEvent.CancelClicked) }) {
            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.common_close))
          }
        },
        actions = {
          if (state is SongEditUiState.Content) {
            if (state.isSaving) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp).padding(end = 12.dp),
                strokeWidth = 2.dp
              )
            } else {
              IconButton(
                onClick = { onEvent(SongEditEvent.SaveClicked) },
                enabled = state.hasUnsavedChanges
              ) {
                Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.tags_save))
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    when (state) {
      SongEditUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = stringResource(Res.string.song_edit_loading)
        )
      }

      is SongEditUiState.Content -> {
        EditForm(
          state = state,
          onEvent = onEvent,
          modifier = Modifier.padding(innerPadding)
        )
      }

      is SongEditUiState.Error -> {
        val message = if (state.message == "SONG_NOT_FOUND") {
          stringResource(Res.string.song_edit_error_not_found)
        } else {
          state.message
        }
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = stringResource(Res.string.common_error_title),
          message = message
        )
      }
    }
  }

  // Discard changes dialog
  if (showDiscardDialog) {
    DiscardChangesDialog(
      onConfirm = { onEvent(SongEditEvent.DiscardChangesConfirmed) },
      onDismiss = { onEvent(SongEditEvent.DismissDiscardDialog) }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditForm(
  state: SongEditUiState.Content,
  onEvent: (SongEditEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .imePadding()
      .padding(MaterialTheme.spacing.screenHorizontal)
  ) {
  val validationErrorText = when (val m = state.validationMessage) {
    SongEditValidationMessage.TitleRequired -> stringResource(Res.string.song_edit_validation_title_required)
    SongEditValidationMessage.TextRequired -> stringResource(Res.string.song_edit_validation_text_required)
    is SongEditValidationMessage.SaveError -> stringResource(Res.string.song_edit_save_error_prefix, m.details ?: "")
    null -> null
  }

    // Error message
    validationErrorText?.let { error ->
      Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm)
      )
    }

    // Title field
    OutlinedTextField(
      value = state.title,
      onValueChange = { onEvent(SongEditEvent.TitleChanged(it)) },
      label = { Text(stringResource(Res.string.song_edit_label_title)) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      isError = state.validationMessage != null && state.title.isBlank()
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Number field
    OutlinedTextField(
      value = state.number,
      onValueChange = { onEvent(SongEditEvent.NumberChanged(it)) },
      label = { Text(stringResource(Res.string.song_edit_label_number)) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Tags section
    if (state.allTags.isNotEmpty()) {
      Text(
        text = stringResource(Res.string.song_edit_label_tags),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(Modifier.height(MaterialTheme.spacing.sm))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
      ) {
        state.allTags.forEach { tag ->
          FilterChip(
            selected = tag.isSelected,
            onClick = { onEvent(SongEditEvent.TagToggled(tag.id)) },
            label = {
              Text(tag.name)
            },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(tag.color)
              )
            },
            trailingIcon = if (tag.isSelected) {
              {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
              }
            } else null
          )
        }
      }

      Spacer(Modifier.height(MaterialTheme.spacing.lg))
    }

    // Text field
    OutlinedTextField(
      value = state.text,
      onValueChange = { onEvent(SongEditEvent.TextChanged(it)) },
      label = { Text(stringResource(Res.string.song_edit_label_text)) },
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
      isError = state.validationMessage != null && state.text.isBlank()
    )

    Spacer(Modifier.height(MaterialTheme.spacing.xxl))
  }
}

@Composable
private fun DiscardChangesDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.song_edit_discard_title)) },
    text = { Text(stringResource(Res.string.song_edit_discard_message)) },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(stringResource(Res.string.song_edit_discard_confirm), color = MaterialTheme.colorScheme.error)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.song_edit_discard_cancel))
      }
    }
  )
}

