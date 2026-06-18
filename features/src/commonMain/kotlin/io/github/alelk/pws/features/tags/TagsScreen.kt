package io.github.alelk.pws.features.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import io.github.alelk.pws.features.components.testTagsAsResourceId

import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.OnTabReselected
import io.github.alelk.pws.features.components.StateCrossfade
import io.github.alelk.pws.features.components.confirm
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.common_delete
import io.github.alelk.pws.features.resources.common_error_title
import io.github.alelk.pws.features.resources.tags_add
import io.github.alelk.pws.features.resources.tags_cancel
import io.github.alelk.pws.features.resources.tags_color_selected
import io.github.alelk.pws.features.resources.tags_delete
import io.github.alelk.pws.features.resources.tags_delete_dialog_message
import io.github.alelk.pws.features.resources.tags_delete_dialog_title
import io.github.alelk.pws.features.resources.tags_dialog_color
import io.github.alelk.pws.features.resources.tags_dialog_edit_title
import io.github.alelk.pws.features.resources.tags_dialog_name
import io.github.alelk.pws.features.resources.tags_dialog_new_title
import io.github.alelk.pws.features.resources.tags_edit
import io.github.alelk.pws.features.resources.tags_empty_subtitle
import io.github.alelk.pws.features.resources.tags_empty_title
import io.github.alelk.pws.features.resources.tags_hide
import io.github.alelk.pws.features.resources.tags_hide_dialog_message
import io.github.alelk.pws.features.resources.tags_hide_dialog_title
import io.github.alelk.pws.features.resources.tags_loading
import io.github.alelk.pws.features.resources.tags_save
import io.github.alelk.pws.features.resources.tags_snackbar_created
import io.github.alelk.pws.features.resources.tags_snackbar_deleted
import io.github.alelk.pws.features.resources.tags_snackbar_error_prefix
import io.github.alelk.pws.features.resources.tags_snackbar_hidden
import io.github.alelk.pws.features.resources.tags_snackbar_updated
import io.github.alelk.pws.features.resources.tags_title
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// Predefined color palette for tags
private val tagColors = listOf(
  Color(0xFFE57373), // Red
  Color(0xFFBA68C8), // Purple
  Color(0xFF7986CB), // Indigo
  Color(0xFF4FC3F7), // Light Blue
  Color(0xFF4DB6AC), // Teal
  Color(0xFF81C784), // Green
  Color(0xFFFFD54F), // Amber
  Color(0xFFFF8A65), // Deep Orange
  Color(0xFFA1887F), // Brown
  Color(0xFF90A4AE), // Blue Grey
)

class TagsScreen : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<TagsScreenModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navigator = LocalNavigator.currentOrThrow

    // Handle navigation effects
    LaunchedEffect(Unit) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is TagsScreenModel.Effect.NavigateToTagSongs -> {
            val screen = ScreenRegistry.get(SharedScreens.tagSongs(effect.tag.id))
            navigator.push(screen)
          }
          is TagsScreenModel.Effect.ShowSnackbar -> {
            val message = when (val m = effect.message) {
              TagsScreenModel.SnackbarMessage.Updated -> getString(Res.string.tags_snackbar_updated)
              TagsScreenModel.SnackbarMessage.Created -> getString(Res.string.tags_snackbar_created)
              TagsScreenModel.SnackbarMessage.Hidden -> getString(Res.string.tags_snackbar_hidden)
              TagsScreenModel.SnackbarMessage.Deleted -> getString(Res.string.tags_snackbar_deleted)
              is TagsScreenModel.SnackbarMessage.Error -> {
                val details = m.details ?: ""
                getString(Res.string.tags_snackbar_error_prefix, details)
              }
            }
            snackbarHostState.showSnackbar(message)
          }
        }
      }
    }

    TagsContent(
      state = state,
      snackbarHostState = snackbarHostState,
      onEvent = viewModel::onEvent,
      onRetry = viewModel::retry,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsContent(
  state: TagsUiState,
  snackbarHostState: SnackbarHostState,
  onEvent: (TagsEvent) -> Unit,
  onRetry: () -> Unit = {},
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val navigator = LocalNavigator.currentOrThrow
  val haptic = LocalHapticFeedback.current
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  OnTabReselected(NavDestination.Tags) {
    scope.launch { listState.animateScrollToItem(0) }
    scrollBehavior.state.heightOffset = 0f
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.tags_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
          )
        },
        actions = {
          IconButton(
              onClick = { navigator.push(ScreenRegistry.get(SharedScreens.Settings)) },
              modifier = Modifier.testTag("action:open-settings")
            ) {
              Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(Res.string.settings_open)
              )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      )
    },
    floatingActionButton = {
      if (state is TagsUiState.Content || state is TagsUiState.Empty) {
        FloatingActionButton(
          onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onEvent(TagsEvent.AddTagClicked)
          },
          modifier = Modifier.testTag("action:add-tag")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
          )
        }
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    StateCrossfade(state, modifier = Modifier.padding(innerPadding)) { current ->
      when (current) {
        TagsUiState.Loading -> {
          LoadingContent(message = stringResource(Res.string.tags_loading))
        }

        TagsUiState.Empty -> {
          EmptyContent(
            icon = Icons.Outlined.Tag,
            title = stringResource(Res.string.tags_empty_title),
            subtitle = stringResource(Res.string.tags_empty_subtitle)
          )
        }

        is TagsUiState.Content -> {
          TagsList(
            tags = current.tags,
            listState = listState,
            onTagClick = { onEvent(TagsEvent.TagClicked(it)) },
            onEditClick = { onEvent(TagsEvent.EditTag(it)) },
            onDeleteClick = { onEvent(TagsEvent.DeleteTag(it)) }
          )

          // Add/Edit dialog
          if (current.showAddDialog) {
            TagDialog(
              editingTag = current.editingTag,
              onSave = { name, color -> onEvent(TagsEvent.SaveTag(name, color)) },
              onDismiss = { onEvent(TagsEvent.DismissDialog) }
            )
          }

          // Delete confirmation
          current.showDeleteConfirmation?.let { tag ->
            DeleteTagDialog(
              tag = tag,
              onConfirm = { onEvent(TagsEvent.ConfirmDeleteTag(tag)) },
              onDismiss = { onEvent(TagsEvent.DismissDeleteConfirmation) }
            )
          }
        }

        is TagsUiState.Error -> {
          ErrorContent(
            title = stringResource(Res.string.common_error_title),
            message = current.message,
            onRetry = onRetry,
          )
        }
      }
    }
  }
}

@Composable
private fun TagsList(
  tags: List<TagUi>,
  modifier: Modifier = Modifier,
  listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
  onTagClick: (TagUi) -> Unit,
  onEditClick: (TagUi) -> Unit,
  onDeleteClick: (TagUi) -> Unit
) {
  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.sm)
  ) {
    items(
      items = tags,
      key = { it.id.toString() }
    ) { tag ->
      TagListItem(
        tag = tag,
        onClick = { onTagClick(tag) },
        onEditClick = { onEditClick(tag) },
        onDeleteClick = { onDeleteClick(tag) }
      )

      if (tag != tags.last()) {
        HorizontalDivider(
          modifier = Modifier.padding(start = 56.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
      }
    }

    item {
      Spacer(Modifier.height(80.dp))
    }
  }
}

@Composable
private fun TagListItem(
  tag: TagUi,
  onClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tag-row-${tag.name}")
      .clickable(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = MaterialTheme.spacing.listItemHorizontal,
          vertical = MaterialTheme.spacing.listItemVertical
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Color indicator
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(tag.color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(tag.color)
        )
      }

      Spacer(Modifier.width(MaterialTheme.spacing.md))

      // Name
      Text(
        text = tag.name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )

      // Actions for all tags (API handles user overrides for predefined tags)
      IconButton(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onEditClick()
      }) {
        Icon(
          imageVector = Icons.Outlined.Edit,
          contentDescription = stringResource(Res.string.tags_edit),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      IconButton(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onDeleteClick()
      }) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = stringResource(Res.string.tags_delete),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagDialog(
  editingTag: TagUi?,
  onSave: (String, Color) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember(editingTag) { mutableStateOf(editingTag?.name ?: "") }
  var selectedColor by remember(editingTag) {
    mutableStateOf(editingTag?.color ?: tagColors.first())
  }
  val haptic = LocalHapticFeedback.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        if (editingTag != null) {
          stringResource(Res.string.tags_dialog_edit_title)
        } else {
          stringResource(Res.string.tags_dialog_new_title)
        }
      )
    },
    text = {
      Column(modifier = Modifier.testTagsAsResourceId()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(Res.string.tags_dialog_name)) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("field:tag-name")
        )

        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        Text(
          text = stringResource(Res.string.tags_dialog_color),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
          tagColors.forEach { color ->
            ColorOption(
              color = color,
              isSelected = color == selectedColor,
              onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedColor = color 
              }
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { 
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onSave(name, selectedColor)
        },
        modifier = Modifier.testTagsAsResourceId().testTag("action:save-tag"),
        enabled = name.isNotBlank()
      ) {
        Text(stringResource(Res.string.tags_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.tags_cancel))
      }
    }
  )
}

@Composable
private fun ColorOption(
  color: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(40.dp)
      .clip(CircleShape)
      .background(color)
      .then(
        if (isSelected) {
          Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        } else Modifier
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (isSelected) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = stringResource(Res.string.tags_color_selected),
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun DeleteTagDialog(
  tag: TagUi,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  val isPredefined = tag.isPredefined
  val title = if (isPredefined) stringResource(Res.string.tags_hide_dialog_title) else stringResource(Res.string.tags_delete_dialog_title)
  val message = if (isPredefined) stringResource(Res.string.tags_hide_dialog_message, tag.name) else stringResource(Res.string.tags_delete_dialog_message, tag.name)
  val confirmText = if (isPredefined) stringResource(Res.string.tags_hide) else stringResource(Res.string.common_delete)
  val haptic = LocalHapticFeedback.current

  io.github.alelk.pws.features.components.AppConfirmDialog(
    title = title,
    message = message,
    confirmLabel = confirmText,
    icon = Icons.Outlined.Delete,
    onConfirm = {
      haptic.confirm()
      onConfirm()
    },
    onDismiss = onDismiss,
  )
}
