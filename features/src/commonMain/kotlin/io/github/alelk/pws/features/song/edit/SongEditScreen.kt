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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import io.github.alelk.pws.features.theme.spacing
import org.koin.core.parameter.parametersOf

class SongEditScreen(val songId: SongId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongEditScreenModel>(parameters = { parametersOf(songId) })
    val state by viewModel.state.collectAsState()
    val showDiscardDialog by viewModel.showDiscardDialog.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    SongEditContent(
      state = state,
      showDiscardDialog = showDiscardDialog,
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
  onEvent: (SongEditEvent) -> Unit,
  onNavigateBack: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Редактирование") },
        navigationIcon = {
          IconButton(onClick = { onEvent(SongEditEvent.CancelClicked) }) {
            Icon(Icons.Default.Close, contentDescription = "Отмена")
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
                Icon(Icons.Default.Check, contentDescription = "Сохранить")
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
          message = "Загрузка..."
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
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Ошибка",
          message = state.message
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
    // Error message
    state.validationError?.let { error ->
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
      label = { Text("Название") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      isError = state.validationError != null && state.title.isBlank()
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Number field
    OutlinedTextField(
      value = state.number,
      onValueChange = { onEvent(SongEditEvent.NumberChanged(it)) },
      label = { Text("Номер") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(MaterialTheme.spacing.md))

    // Tags section
    if (state.allTags.isNotEmpty()) {
      Text(
        text = "Теги",
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
      label = { Text("Текст песни") },
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
      isError = state.validationError != null && state.text.isBlank()
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
    title = { Text("Отменить изменения?") },
    text = { Text("Все несохранённые изменения будут потеряны.") },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text("Отменить", color = MaterialTheme.colorScheme.error)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Продолжить редактирование")
      }
    }
  )
}

