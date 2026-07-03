package io.github.alelk.pws.features.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.features.components.NavDestination
import io.github.alelk.pws.features.components.OnTabReselected
import io.github.alelk.pws.features.favorites.FavoritesBody
import io.github.alelk.pws.features.favorites.FavoritesEvent
import io.github.alelk.pws.features.favorites.FavoritesScreenModel
import io.github.alelk.pws.features.favorites.FavoritesSortSheet
import io.github.alelk.pws.features.favorites.FavoritesUiState
import io.github.alelk.pws.features.history.ClearHistoryDialog
import io.github.alelk.pws.features.history.HistoryBody
import io.github.alelk.pws.features.history.HistoryEvent
import io.github.alelk.pws.features.history.HistoryScreenModel
import io.github.alelk.pws.features.history.HistoryUiState
import io.github.alelk.pws.features.components.confirm
import io.github.alelk.pws.features.resources.Res
import io.github.alelk.pws.features.resources.favorites_sort
import io.github.alelk.pws.features.resources.favorites_sort_direction
import io.github.alelk.pws.features.resources.history_clear
import io.github.alelk.pws.features.resources.nav_favorites
import io.github.alelk.pws.features.resources.nav_history
import io.github.alelk.pws.features.resources.nav_library
import io.github.alelk.pws.features.resources.nav_tags
import io.github.alelk.pws.features.resources.settings_open
import io.github.alelk.pws.features.song.detail.LocalFavoritesDisplaySettings
import io.github.alelk.pws.features.tags.HandleTagsEffects
import io.github.alelk.pws.features.tags.TagsBody
import io.github.alelk.pws.features.tags.TagsEvent
import io.github.alelk.pws.features.tags.TagsScreenModel
import io.github.alelk.pws.features.tags.TagsUiState
import io.github.alelk.pws.features.theme.spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Sections of the Library tab, shown under the segmented switcher. */
internal enum class LibrarySection(val labelRes: StringResource) {
  Favorites(Res.string.nav_favorites),
  History(Res.string.nav_history),
  Tags(Res.string.nav_tags),
}

/**
 * Library tab: Favorites + History + Tags under one segmented switcher
 * (design system, frame 8 — "three sections under one tab").
 */
class LibraryScreen : Screen {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val favoritesModel = koinScreenModel<FavoritesScreenModel>()
    val historyModel = koinScreenModel<HistoryScreenModel>()
    val tagsModel = koinScreenModel<TagsScreenModel>()

    val favoritesState by favoritesModel.state.collectAsState()
    val historyState by historyModel.state.collectAsState()
    val tagsState by tagsModel.state.collectAsState()
    val showClearDialog by historyModel.showClearDialog.collectAsState()

    val displaySettings = LocalFavoritesDisplaySettings.current
    LaunchedEffect(displaySettings) {
      displaySettings?.let { favoritesModel.setDisplaySettings(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    HandleTagsEffects(tagsModel, snackbarHostState)

    var section by rememberSaveable { mutableStateOf(LibrarySection.Favorites.ordinal) }
    val currentSection = LibrarySection.entries[section]

    val navigator = LocalNavigator.currentOrThrow
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var showSortSheet by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val favoritesListState = rememberLazyListState()
    val historyListState = rememberLazyListState()
    val tagsListState = rememberLazyListState()

    OnTabReselected(NavDestination.Library) {
      val listState = when (currentSection) {
        LibrarySection.Favorites -> favoritesListState
        LibrarySection.History -> historyListState
        LibrarySection.Tags -> tagsListState
      }
      scope.launch { listState.animateScrollToItem(0) }
      scrollBehavior.state.heightOffset = 0f
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        LargeTopAppBar(
          title = {
            Text(
              text = stringResource(Res.string.nav_library),
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
            when (currentSection) {
              LibrarySection.Favorites -> {
                val state = favoritesState
                if (state is FavoritesUiState.Content) {
                  IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSortSheet = true
                  }) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Sort,
                      contentDescription = stringResource(Res.string.favorites_sort)
                    )
                  }
                  IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    favoritesModel.onEvent(FavoritesEvent.ToggleSortDirection)
                  }) {
                    Icon(
                      imageVector = if (state.ascending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                      contentDescription = stringResource(Res.string.favorites_sort_direction)
                    )
                  }
                }
              }

              LibrarySection.History -> {
                val state = historyState
                if (state is HistoryUiState.Content && state.canClearAll) {
                  IconButton(
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      historyModel.onEvent(HistoryEvent.ClearAll)
                    },
                    modifier = Modifier.testTag("action:clear-history")
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.DeleteSweep,
                      contentDescription = stringResource(Res.string.history_clear)
                    )
                  }
                }
              }

              LibrarySection.Tags -> Unit
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
        if (currentSection == LibrarySection.Tags && (tagsState is TagsUiState.Content || tagsState is TagsUiState.Empty)) {
          FloatingActionButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              tagsModel.onEvent(TagsEvent.AddTagClicked)
            },
            modifier = Modifier.testTag("action:add-tag")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
          }
        }
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
      Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        LibrarySegmentedControl(
          selected = currentSection,
          onSelect = { newSection ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            section = newSection.ordinal
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = MaterialTheme.spacing.screenHorizontal,
              vertical = MaterialTheme.spacing.sm
            )
        )

        when (currentSection) {
          LibrarySection.Favorites -> FavoritesBody(
            state = favoritesState,
            listState = favoritesListState,
            onRemove = { favoritesModel.onEvent(FavoritesEvent.RemoveFromFavorites(it)) },
          )

          LibrarySection.History -> HistoryBody(
            state = historyState,
            listState = historyListState,
            onRemoveItem = { historyModel.onEvent(HistoryEvent.RemoveItem(it)) },
            onRetry = historyModel::retry,
          )

          LibrarySection.Tags -> TagsBody(
            state = tagsState,
            listState = tagsListState,
            onEvent = tagsModel::onEvent,
            onRetry = tagsModel::retry,
          )
        }
      }
    }

    val favState = favoritesState
    if (showSortSheet && favState is FavoritesUiState.Content) {
      FavoritesSortSheet(
        sortMode = favState.sortMode,
        onSortModeChange = { favoritesModel.onEvent(FavoritesEvent.ChangeSortMode(it)) },
        onDismiss = { showSortSheet = false }
      )
    }

    if (showClearDialog) {
      ClearHistoryDialog(
        onConfirm = {
          haptic.confirm()
          historyModel.onEvent(HistoryEvent.ConfirmClearAll)
        },
        onDismiss = { historyModel.onEvent(HistoryEvent.DismissClearDialog) }
      )
    }
  }
}

/**
 * Segmented switcher per the design system: pill container on a warm tint,
 * selected segment — conifer accent.
 */
@Composable
private fun LibrarySegmentedControl(
  selected: LibrarySection,
  onSelect: (LibrarySection) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
      .padding(3.dp)
  ) {
    LibrarySection.entries.forEach { section ->
      val isSelected = section == selected
      val background by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
        label = "library-segment-bg",
      )
      val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "library-segment-text",
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(9.dp))
          .background(background)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onSelect(section) }
          )
          .padding(vertical = MaterialTheme.spacing.sm)
          .testTag("library-segment-${section.name.lowercase()}"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stringResource(section.labelRes),
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400
          ),
          color = textColor,
          maxLines = 1
        )
      }
    }
  }
}
