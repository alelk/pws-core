package io.github.alelk.pws.features.song.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import io.github.alelk.pws.features.resources.song_detail_info_author
import io.github.alelk.pws.features.resources.song_detail_info_bible
import io.github.alelk.pws.features.resources.song_detail_info_composer
import io.github.alelk.pws.features.resources.song_detail_info_translator
import io.github.alelk.pws.features.resources.song_detail_info_year
import io.github.alelk.pws.features.resources.song_detail_tags
import io.github.alelk.pws.features.resources.label
import io.github.alelk.pws.features.resources.song_edit_discard_cancel
import io.github.alelk.pws.features.resources.song_edit_discard_confirm
import io.github.alelk.pws.features.resources.song_edit_discard_message
import io.github.alelk.pws.features.resources.song_edit_discard_title
import io.github.alelk.pws.features.resources.song_edit_error_not_found
import io.github.alelk.pws.features.resources.song_edit_label_number
import io.github.alelk.pws.features.resources.song_edit_label_tags
import io.github.alelk.pws.features.resources.song_edit_label_tonalities
import io.github.alelk.pws.features.resources.song_edit_label_text
import io.github.alelk.pws.features.resources.song_edit_label_title
import io.github.alelk.pws.features.resources.song_edit_loading
import io.github.alelk.pws.features.resources.song_edit_save_error_prefix
import io.github.alelk.pws.features.resources.song_edit_save_success
import io.github.alelk.pws.features.resources.song_edit_title
import io.github.alelk.pws.features.resources.song_edit_validation_text_required
import io.github.alelk.pws.features.resources.song_edit_validation_title_required
import io.github.alelk.pws.features.resources.song_edit_validation_text_format_invalid
import io.github.alelk.pws.features.resources.tags_save
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class SongEditScreen(val songIdLong: Long) : Screen {
  val songId: SongId get() = SongId(songIdLong)

  override val key: String = "song-edit/$songIdLong"

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
  val haptic = LocalHapticFeedback.current
  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = { Text(stringResource(Res.string.song_edit_title), modifier = Modifier.semantics { heading() }) },
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
                onClick = { 
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onEvent(SongEditEvent.SaveClicked) 
                },
                enabled = state.hasUnsavedChanges,
                modifier = Modifier.testTag("action:save")
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
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
        val message = when (val m = state.message) {
          io.github.alelk.pws.features.app.UiMessage.SongNotFound ->
            stringResource(Res.string.song_edit_error_not_found)
          else -> io.github.alelk.pws.features.app.rememberResolved(m)
        }
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = stringResource(Res.string.common_error_title),
          message = message,
        )
      }
    }
  }

  // Discard changes dialog
  if (showDiscardDialog) {
    DiscardChangesDialog(
      onConfirm = { 
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onEvent(SongEditEvent.DiscardChangesConfirmed) 
      },
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
  val haptic = LocalHapticFeedback.current
  val highlightColor = MaterialTheme.colorScheme.primary
  val visualTransformation = remember(state.locale, highlightColor) {
    LyricVisualTransformation(state.locale, highlightColor)
  }

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
      is SongEditValidationMessage.InvalidTextFormat -> stringResource(Res.string.song_edit_validation_text_format_invalid, m.details ?: "")
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
      modifier = Modifier
        .fillMaxWidth()
        .testTag("field:song-edit-title"),
      singleLine = true,
      isError = state.validationMessage != null && state.title.isBlank()
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Author
    OutlinedTextField(
      value = state.author,
      onValueChange = { onEvent(SongEditEvent.AuthorChanged(it)) },
      label = { Text(stringResource(Res.string.song_detail_info_author)) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Composer
    OutlinedTextField(
      value = state.composer,
      onValueChange = { onEvent(SongEditEvent.ComposerChanged(it)) },
      label = { Text(stringResource(Res.string.song_detail_info_composer)) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Translator
    OutlinedTextField(
      value = state.translator,
      onValueChange = { onEvent(SongEditEvent.TranslatorChanged(it)) },
      label = { Text(stringResource(Res.string.song_detail_info_translator)) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
    ) {
      // Year
      OutlinedTextField(
        value = state.year,
        onValueChange = { onEvent(SongEditEvent.YearChanged(it)) },
        label = { Text(stringResource(Res.string.song_detail_info_year)) },
        modifier = Modifier.weight(1f),
        singleLine = true
      )

      // Bible Ref
      OutlinedTextField(
        value = state.bibleRef,
        onValueChange = { onEvent(SongEditEvent.BibleRefChanged(it)) },
        label = { Text(stringResource(Res.string.song_detail_info_bible)) },
        modifier = Modifier.weight(1f),
        singleLine = true
      )
    }

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Text field
    OutlinedTextField(
      value = state.text,
      onValueChange = { onEvent(SongEditEvent.TextChanged(it)) },
      label = { Text(stringResource(Res.string.song_edit_label_text)) },
      modifier = Modifier
        .fillMaxWidth()
        .height(400.dp),
      isError = state.validationMessage != null && state.text.isBlank(),
      visualTransformation = visualTransformation
    )

    Spacer(Modifier.height(MaterialTheme.spacing.lg))

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
            onClick = { 
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onEvent(SongEditEvent.TagToggled(tag.id)) 
            },
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

    // Tonalities section
    Text(
      text = stringResource(Res.string.song_edit_label_tonalities),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(MaterialTheme.spacing.sm))

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
      io.github.alelk.pws.domain.tonality.Tonality.entries.forEach { tonality ->
        val isSelected = tonality in state.tonalities
        FilterChip(
          selected = isSelected,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onEvent(SongEditEvent.TonalityToggled(tonality))
          },
          label = {
            Text(stringResource(tonality.label))
          },
          trailingIcon = if (isSelected) {
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

    Spacer(Modifier.height(MaterialTheme.spacing.xxl))
  }
}

@Composable
private fun DiscardChangesDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  io.github.alelk.pws.features.components.AppConfirmDialog(
    title = stringResource(Res.string.song_edit_discard_title),
    message = stringResource(Res.string.song_edit_discard_message),
    confirmLabel = stringResource(Res.string.song_edit_discard_confirm),
    dismissLabel = stringResource(Res.string.song_edit_discard_cancel),
    onConfirm = onConfirm,
    onDismiss = onDismiss,
  )
}
