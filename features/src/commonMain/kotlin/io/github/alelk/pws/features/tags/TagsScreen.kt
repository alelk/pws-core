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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.registry.rememberScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.components.EmptyContent
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.theme.spacing

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
            val screen = ScreenRegistry.get(SharedScreens.TagSongs(effect.tag.id))
            navigator.push(screen)
          }
          is TagsScreenModel.Effect.ShowSnackbar -> {
            snackbarHostState.showSnackbar(effect.message)
          }
        }
      }
    }

    TagsContent(
      state = state,
      snackbarHostState = snackbarHostState,
      onEvent = viewModel::onEvent
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsContent(
  state: TagsUiState,
  snackbarHostState: SnackbarHostState,
  onEvent: (TagsEvent) -> Unit
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            text = "Категории",
            style = MaterialTheme.typography.headlineMedium
          )
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
          onClick = { onEvent(TagsEvent.AddTagClicked) }
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Добавить категорию"
          )
        }
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    when (state) {
      TagsUiState.Loading -> {
        LoadingContent(
          modifier = Modifier.padding(innerPadding),
          message = "Загрузка категорий..."
        )
      }

      TagsUiState.Empty -> {
        EmptyContent(
          modifier = Modifier.padding(innerPadding),
          icon = Icons.Outlined.Tag,
          title = "Нет категорий",
          subtitle = "Создавайте категории для организации песен"
        )
      }

      is TagsUiState.Content -> {
        TagsList(
          tags = state.tags,
          modifier = Modifier.padding(innerPadding),
          onTagClick = { onEvent(TagsEvent.TagClicked(it)) },
          onEditClick = { onEvent(TagsEvent.EditTag(it)) },
          onDeleteClick = { onEvent(TagsEvent.DeleteTag(it)) }
        )

        // Add/Edit dialog
        if (state.showAddDialog) {
          TagDialog(
            editingTag = state.editingTag,
            onSave = { name, color -> onEvent(TagsEvent.SaveTag(name, color)) },
            onDismiss = { onEvent(TagsEvent.DismissDialog) }
          )
        }

        // Delete confirmation
        state.showDeleteConfirmation?.let { tag ->
          DeleteTagDialog(
            tag = tag,
            onConfirm = { onEvent(TagsEvent.ConfirmDeleteTag(tag)) },
            onDismiss = { onEvent(TagsEvent.DismissDeleteConfirmation) }
          )
        }
      }

      is TagsUiState.Error -> {
        ErrorContent(
          modifier = Modifier.padding(innerPadding),
          title = "Ошибка",
          message = state.message
        )
      }
    }
  }
}

@Composable
private fun TagsList(
  tags: List<TagUi>,
  modifier: Modifier = Modifier,
  onTagClick: (TagUi) -> Unit,
  onEditClick: (TagUi) -> Unit,
  onDeleteClick: (TagUi) -> Unit
) {
  LazyColumn(
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
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
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
      IconButton(onClick = onEditClick) {
        Icon(
          imageVector = Icons.Outlined.Edit,
          contentDescription = "Редактировать",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      IconButton(onClick = onDeleteClick) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Удалить",
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

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (editingTag != null) "Редактировать категорию" else "Новая категория")
    },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Название") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        Text(
          text = "Цвет",
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
              onClick = { selectedColor = color }
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onSave(name, selectedColor) },
        enabled = name.isNotBlank()
      ) {
        Text("Сохранить")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена")
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
        contentDescription = "Выбрано",
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
  val title = if (isPredefined) "Скрыть категорию?" else "Удалить категорию?"
  val message = if (isPredefined) {
    "Категория \"${tag.name}\" будет скрыта. Вы сможете восстановить её позже."
  } else {
    "Категория \"${tag.name}\" будет удалена. Песни не будут затронуты."
  }
  val confirmText = if (isPredefined) "Скрыть" else "Удалить"

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Outlined.Delete,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(title)
    },
    text = {
      Text(message)
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(confirmText, color = MaterialTheme.colorScheme.error)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена")
      }
    }
  )
}

private fun pluralizeSongs(count: Int): String {
  val lastTwo = count % 100
  val lastOne = count % 10
  val word = when {
    lastTwo in 11..19 -> "песен"
    lastOne == 1 -> "песня"
    lastOne in 2..4 -> "песни"
    else -> "песен"
  }
  return "$count $word"
}

