package io.github.alelk.pws.features.song.detail

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alelk.pws.core.navigation.SharedScreens
import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.song.lyric.Bridge
import io.github.alelk.pws.domain.song.lyric.Chorus
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.LyricPart
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.songreference.usecase.SongReferenceDetail
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.features.components.AppModalBottomSheet
import io.github.alelk.pws.features.components.AppTopBar
import io.github.alelk.pws.features.components.ErrorContent
import io.github.alelk.pws.features.components.LoadingContent
import io.github.alelk.pws.features.resources.*
import io.github.alelk.pws.features.song.edit.SongEditScreen
import io.github.alelk.pws.features.theme.spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class SongDetailScreen(val songNumberId: SongNumberId) : Screen {
  override val key: String = "song-detail/${songNumberId.identifier}"

  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongDetailScreenModel>(parameters = { parametersOf(songNumberId) })
    val state by viewModel.state.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val bookSongNumberIds by viewModel.bookSongNumberIds.collectAsState()
    val currentSongNumberId by viewModel.currentSongNumberId.collectAsState()
    val references by viewModel.references.collectAsState()
    val referenceBookContexts by viewModel.referenceBookContexts.collectAsState()
    val songTags by viewModel.songTags.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val bookNumberMap by viewModel.bookNumberMap.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(Unit) {
      viewModel.effects.collect { effect ->
        when (effect) {
          is SongDetailScreenModel.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
        }
      }
    }

    val donationBoostyUrl = (state as? SongDetailUiState.Content)?.donationBoostyUrl ?: ""

    if (bookSongNumberIds.size > 1) {
      val initialPage = bookSongNumberIds.indexOf(currentSongNumberId).coerceAtLeast(0)
      SongDetailPager(
        snackbarHostState = snackbarHostState,
        state = state,
        isFavorite = isFavorite,
        currentSongNumberId = currentSongNumberId,
        bookSongNumberIds = bookSongNumberIds,
        initialPage = initialPage,
        references = references,
        referenceBookContexts = referenceBookContexts,
        currentBookId = currentSongNumberId.bookId,
        songTags = songTags,
        allTags = allTags,
        bookNumberMap = bookNumberMap,
        onFavoriteClick = { viewModel.onToggleFavorite() },
        onSaveTags = { viewModel.onSaveTags(it) },
        onJumpToNumber = { viewModel.resolveNumber(it) },
        onPageChanged = { viewModel.onPageChanged(it) },
        onDonationDonate = {
          viewModel.onDonationClicked()
          if (donationBoostyUrl.isNotBlank()) uriHandler.openUri(donationBoostyUrl)
        },
        onDonationDismiss = { viewModel.onDonationDismissed() },
      )
    } else {
      SongDetailContent(
        snackbarHostState = snackbarHostState,
        state = state,
        isFavorite = isFavorite,
        references = references,
        referenceBookContexts = referenceBookContexts,
        currentBookId = currentSongNumberId.bookId,
        songTags = songTags,
        allTags = allTags,
        bookNumberMap = bookNumberMap,
        onFavoriteClick = { viewModel.onToggleFavorite() },
        onSaveTags = { viewModel.onSaveTags(it) },
        onJumpToNumber = { viewModel.resolveNumber(it) },
        onDonationDonate = {
          viewModel.onDonationClicked()
          if (donationBoostyUrl.isNotBlank()) uriHandler.openUri(donationBoostyUrl)
        },
        onDonationDismiss = { viewModel.onDonationDismissed() },
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Pager — navigate left/right between all songs in the book
// Uses [SongPager] which adapts to the platform:
// - Mobile: [HorizontalPager] for native swipe support.
// - Desktop/Web: [AnimatedContent] for stability (no swipe).
// ---------------------------------------------------------------------------

@Composable
private fun SongDetailPager(
  snackbarHostState: SnackbarHostState,
  state: SongDetailUiState,
  isFavorite: Boolean,
  currentSongNumberId: SongNumberId,
  bookSongNumberIds: List<SongNumberId>,
  initialPage: Int,
  references: List<SongReferenceDetail>,
  referenceBookContexts: Map<io.github.alelk.pws.domain.core.ids.SongId, List<SongDetailScreenModel.ReferenceBookContextUi>>,
  currentBookId: io.github.alelk.pws.domain.core.ids.BookId,
  songTags: List<Tag<TagId>>,
  allTags: List<Tag<TagId>>,
  bookNumberMap: Map<Int, SongNumberId>,
  onFavoriteClick: () -> Unit,
  onSaveTags: (Set<TagId>) -> Unit,
  onJumpToNumber: (Int) -> SongNumberId?,
  onPageChanged: (SongNumberId) -> Unit,
  onDonationDonate: () -> Unit = {},
  onDonationDismiss: () -> Unit = {},
) {
  var currentPage by remember { mutableIntStateOf(initialPage.coerceIn(0, bookSongNumberIds.lastIndex)) }
  val focusRequester = remember { FocusRequester() }

  // Sync page → ScreenModel
  LaunchedEffect(currentPage) {
    val id = bookSongNumberIds.getOrNull(currentPage) ?: return@LaunchedEffect
    onPageChanged(id)
  }

  // Sync ScreenModel → page (ensure pager matches current ID from VM)
  LaunchedEffect(currentSongNumberId) {
    val index = bookSongNumberIds.indexOf(currentSongNumberId)
    if (index >= 0 && index != currentPage) {
      currentPage = index
    }
  }

  // Request focus so that keyboard arrow keys are captured
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  SongPager(
    pageCount = bookSongNumberIds.size,
    currentPage = currentPage,
    onPageChanged = { currentPage = it },
    modifier = Modifier
      .fillMaxSize()
      .focusRequester(focusRequester)
      .onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown) {
          when (event.key) {
            Key.DirectionLeft, Key.NavigatePrevious -> {
              if (currentPage > 0) currentPage--
              true
            }
            Key.DirectionRight, Key.NavigateNext -> {
              if (currentPage < bookSongNumberIds.lastIndex) currentPage++
              true
            }
            else -> false
          }
        } else false
      }
  ) { page, onNavigatePrev, onNavigateNext ->
    val animPageId = bookSongNumberIds.getOrNull(page) ?: bookSongNumberIds.first()
    val isCurrentPage = animPageId == currentSongNumberId
    val fallbackContext = SongDetailUiState.DisplayContext(
      songNumber = bookNumberMap.entries.firstOrNull { it.value == animPageId }?.key,
      bookTitle = (state as? SongDetailUiState.Content)?.context?.bookTitle
    )

    SongDetailContent(
      snackbarHostState = snackbarHostState,
      state = if (isCurrentPage) state else SongDetailUiState.Loading,
      isFavorite = if (isCurrentPage) isFavorite else false,
      references = if (isCurrentPage) references else emptyList(),
      referenceBookContexts = if (isCurrentPage) referenceBookContexts else emptyMap(),
      currentBookId = currentBookId,
      songTags = if (isCurrentPage) songTags else emptyList(),
      allTags = allTags,
      bookNumberMap = bookNumberMap,
      onFavoriteClick = onFavoriteClick,
      onSaveTags = onSaveTags,
      onJumpToNumber = onJumpToNumber,
      displayContextOverride = if (isCurrentPage) null else fallbackContext,
      onNavigatePrev = onNavigatePrev,
      onNavigateNext = onNavigateNext,
      onDonationDonate = onDonationDonate,
      onDonationDismiss = onDonationDismiss,
    )
  }
}

// ---------------------------------------------------------------------------
// Content
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailContent(
  state: SongDetailUiState,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  isFavorite: Boolean = false,
  references: List<SongReferenceDetail> = emptyList(),
  referenceBookContexts: Map<io.github.alelk.pws.domain.core.ids.SongId, List<SongDetailScreenModel.ReferenceBookContextUi>> = emptyMap(),
  currentBookId: io.github.alelk.pws.domain.core.ids.BookId? = null,
  songTags: List<Tag<TagId>> = emptyList(),
  allTags: List<Tag<TagId>> = emptyList(),
  bookNumberMap: Map<Int, SongNumberId> = emptyMap(),
  onFavoriteClick: () -> Unit = {},
  onSaveTags: (Set<TagId>) -> Unit = {},
  onJumpToNumber: (Int) -> SongNumberId? = { null },
  displayContextOverride: SongDetailUiState.DisplayContext? = null,
  onNavigatePrev: (() -> Unit)? = null,
  onNavigateNext: (() -> Unit)? = null,
  onDonationDonate: () -> Unit = {},
  onDonationDismiss: () -> Unit = {},
) {
  val navigator = LocalNavigator.currentOrThrow
  val clipboardManager = LocalClipboardManager.current
  val haptic = LocalHapticFeedback.current
  val externalActions = LocalSongDetailExternalActions.current
  val displaySettings = LocalSongDetailDisplaySettings.current
  var fontScale by remember { mutableFloatStateOf(displaySettings?.fontScale ?: 1f) }
  var expandedText by remember { mutableStateOf(displaySettings?.expandedText ?: true) }

  LaunchedEffect(displaySettings?.fontScale) {
    displaySettings?.let { fontScale = it.fontScale }
  }
  LaunchedEffect(displaySettings?.expandedText) {
    displaySettings?.let { expandedText = it.expandedText }
  }

  // Sheet visibility state
  var showTextSettingsSheet by remember { mutableStateOf(false) }
  var showActionsSheet by remember { mutableStateOf(false) }
  var showTagEditorSheet by remember { mutableStateOf(false) }
  var showJumpSheet by remember { mutableStateOf(false) }

  val textSettingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val tagEditorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val jumpSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val songId = (state as? SongDetailUiState.Content)?.song?.id
  val displayContext = when (state) {
    is SongDetailUiState.Content -> state.context
    else -> displayContextOverride ?: SongDetailUiState.DisplayContext()
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      val title = when {
        displayContext.songNumber != null -> "№ ${displayContext.songNumber}"
        state is SongDetailUiState.Content -> state.song.name.value
        else -> stringResource(Res.string.song_detail_title_fallback)
      }

      AppTopBar(
        title = title,
        canNavigateBack = navigator.canPop,
        onNavigateBack = { navigator.pop() },
        actions = {
          if (onNavigatePrev != null) {
            IconButton(onClick = onNavigatePrev) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.song_detail_prev))
            }
          }
          if (onNavigateNext != null) {
            IconButton(onClick = onNavigateNext) {
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(Res.string.song_detail_next))
            }
          }
          IconButton(onClick = { showTextSettingsSheet = true }) {
            Icon(Icons.Filled.FormatSize, contentDescription = stringResource(Res.string.song_detail_text_size))
          }
          // "More actions" button — visible only when content loaded
          if (state is SongDetailUiState.Content) {
            IconButton(onClick = { showActionsSheet = true }, modifier = Modifier.testTag("action:more-actions")) {
              Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
          }
        }
      )
    },
    floatingActionButton = {
      if (state is SongDetailUiState.Content) {
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        FloatingActionButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFavoriteClick()
          },
          containerColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
          contentColor = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
          modifier = Modifier.padding(bottom = bottomInset + 12.dp).testTag("action:toggle-favorite")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (state) {
        SongDetailUiState.Loading -> LoadingContent()
        is SongDetailUiState.Content -> {
          SongContent(
            song = state.song,
            displayContext = state.context,
            fontScale = fontScale,
            expandedText = expandedText,
            references = references,
            referenceBookContexts = referenceBookContexts,
            currentBookId = currentBookId,
            songTags = songTags,
            showDonationCard = state.showDonationCard,
            onDonationDonate = onDonationDonate,
            onDonationDismiss = onDonationDismiss,
          )
        }
        SongDetailUiState.Error -> ErrorContent(
          title = stringResource(Res.string.song_detail_not_found_title),
          message = stringResource(Res.string.song_detail_not_found_message)
        )
      }
    }

    // Text settings sheet
    if (showTextSettingsSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showTextSettingsSheet = false },
        sheetState = textSettingsSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        TextSettingsSheet(
          fontScale = fontScale,
          expandedText = expandedText,
          onFontScaleChange = {
            fontScale = it
            displaySettings?.onFontScaleChange?.invoke(it)
          },
          onExpandedTextChange = {
            expandedText = it
            displaySettings?.onExpandedTextChange?.invoke(it)
          }
        )
      }
    }

    // Actions sheet (Edit / Tags / Jump)
    if (showActionsSheet && songId != null) {
      AppModalBottomSheet(
        onDismissRequest = { showActionsSheet = false },
        sheetState = actionsSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        SongActionsSheet(
          showJump = bookNumberMap.isNotEmpty(),
          onEditSong = {
            showActionsSheet = false
            navigator.push(SongEditScreen(songId))
          },
          onEditTags = {
            showActionsSheet = false
            showTagEditorSheet = true
          },
          onJumpToNumber = {
            showActionsSheet = false
            showJumpSheet = true
          },
          onShare = {
            val shareSong = (state as? SongDetailUiState.Content)?.song ?: return@SongActionsSheet
            externalActions?.shareText?.invoke(buildShareText(shareSong))
          },
          onCopy = {
            val copySong = (state as? SongDetailUiState.Content)?.song ?: return@SongActionsSheet
            clipboardManager.setText(AnnotatedString(buildShareText(copySong)))
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          }
        )
      }
    }

    // Tag editor sheet
    if (showTagEditorSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showTagEditorSheet = false },
        sheetState = tagEditorSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        TagEditorSheet(
          songTags = songTags,
          allTags = allTags,
          onSave = { selectedIds ->
            onSaveTags(selectedIds)
            showTagEditorSheet = false
          },
          onDismiss = { showTagEditorSheet = false }
        )
      }
    }

    // Jump to number sheet
    if (showJumpSheet && bookNumberMap.isNotEmpty()) {
      AppModalBottomSheet(
        onDismissRequest = { showJumpSheet = false },
        sheetState = jumpSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        JumpToNumberSheet(
          bookNumberMap = bookNumberMap,
          onNavigate = { targetId ->
            showJumpSheet = false
            navigator.push(SongDetailScreen(targetId))
          },
          onDismiss = { showJumpSheet = false }
        )
      }
    }
  }
}

@Composable
private fun TextSettingsSheet(
  fontScale: Float,
  expandedText: Boolean,
  onFontScaleChange: (Float) -> Unit,
  onExpandedTextChange: (Boolean) -> Unit,
) {
  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(spacing.lg)
      // Add standard bottom padding for navigation bars
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = stringResource(Res.string.song_detail_text_size),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(spacing.lg))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
      Icon(
        Icons.Filled.FormatSize,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Slider(
        value = fontScale,
        onValueChange = onFontScaleChange,
        valueRange = 0.7f..1.6f,
        steps = 9, // Steps: 0.7, 0.8 ... 1.6
        modifier = Modifier.weight(1f),
        colors = SliderDefaults.colors(
          thumbColor = MaterialTheme.colorScheme.primary,
          activeTrackColor = MaterialTheme.colorScheme.primary,
          inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      )
      Icon(
        Icons.Filled.FormatSize,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(Modifier.height(spacing.md))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = stringResource(Res.string.song_detail_expand_text),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Switch(
        checked = expandedText,
        onCheckedChange = onExpandedTextChange
      )
    }
    Spacer(Modifier.height(spacing.lg))
  }
}

// ---------------------------------------------------------------------------
// Lyric render item — one entry in the ordered rendering list
// ---------------------------------------------------------------------------

private data class LyricRenderItem(
  val part: LyricPart,
  val isRepeat: Boolean,    // second+ occurrence of the same part instance
  val verseNumber: Int?,    // 1-based index for Verse
  val chorusIndex: Int?,    // 1-based index among multiple Chorus parts (null if only one)
  val bridgeIndex: Int?,    // 1-based index among multiple Bridge parts (null if only one)
)

/** Expands a [Lyric] into an ordered list of render items based on [LyricPart.numbers]. */
private fun Lyric.toRenderItems(): List<LyricRenderItem> {
  val maxPos = flatMap { it.numbers }.maxOrNull() ?: return emptyList()
  val posToPartMap: Map<Int, LyricPart> = flatMap { part -> part.numbers.map { pos -> pos to part } }.toMap()

  val verses = filterIsInstance<Verse>()
  val choruses = filterIsInstance<Chorus>()
  val bridges = filterIsInstance<Bridge>()

  val seenParts = mutableSetOf<LyricPart>()
  return (1..maxPos).mapNotNull { pos ->
    val part = posToPartMap[pos] ?: return@mapNotNull null
    val isRepeat = !seenParts.add(part)
    LyricRenderItem(
      part = part,
      isRepeat = isRepeat,
      verseNumber = if (part is Verse) verses.indexOf(part) + 1 else null,
      chorusIndex = if (part is Chorus && choruses.size > 1) choruses.indexOf(part) + 1 else null,
      bridgeIndex = if (part is Bridge && bridges.size > 1) bridges.indexOf(part) + 1 else null,
    )
  }
}

@Composable
private fun SongContent(
  song: SongDetail,
  displayContext: SongDetailUiState.DisplayContext,
  fontScale: Float,
  expandedText: Boolean,
  references: List<SongReferenceDetail> = emptyList(),
  referenceBookContexts: Map<io.github.alelk.pws.domain.core.ids.SongId, List<SongDetailScreenModel.ReferenceBookContextUi>> = emptyMap(),
  currentBookId: io.github.alelk.pws.domain.core.ids.BookId? = null,
  songTags: List<Tag<TagId>> = emptyList(),
  showDonationCard: Boolean = false,
  onDonationDonate: () -> Unit = {},
  onDonationDismiss: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val spacing = MaterialTheme.spacing
  val renderItems = remember(song.lyric) { song.lyric.toRenderItems() }
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(
      top = spacing.lg,
      bottom = spacing.xxl,
      start = spacing.screenHorizontal,
      end = spacing.screenHorizontal
    ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (displayContext.bookTitle != null) {
      item {
        BookContextBanner(displayContext = displayContext)
        Spacer(Modifier.height(spacing.md))
      }
    }

    // Header
    item {
      SongHeader(song)
      Spacer(Modifier.height(spacing.xl))
    }

    // Lyrics
    items(renderItems) { item ->
      LyricPartView(
        part = item.part,
        verseNumber = item.verseNumber,
        chorusIndex = item.chorusIndex,
        bridgeIndex = item.bridgeIndex,
        isRepeat = item.isRepeat,
        fontScale = fontScale,
        expandedText = expandedText
      )
      Spacer(Modifier.height(spacing.lg))
    }

    // Metadata
    item { SongMetadata(song) }

    // Tags — shown after metadata, before cross-references
    if (songTags.isNotEmpty()) {
      item { SongTagsSection(tags = songTags) }
    }

    // Cross-references
    if (references.isNotEmpty()) {
      item {
        SongReferencesSection(
          references = references,
          referenceBookContexts = referenceBookContexts,
          currentBookId = currentBookId,
        )
      }
    }

    // Donation card — shown at the bottom only for loyal users
    if (showDonationCard) {
      item {
        io.github.alelk.pws.features.components.DonationPromptCard(
          onDonate = onDonationDonate,
          onDismiss = onDonationDismiss,
          modifier = Modifier.padding(top = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun BookContextBanner(
  displayContext: SongDetailUiState.DisplayContext
) {
  val bookLabel = displayContext.bookTitle

  if (bookLabel.isNullOrBlank()) return

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f))
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .semantics(mergeDescendants = true) {
          contentDescription = bookLabel
        },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSecondaryContainer
      )

      Text(
        text = bookLabel,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
    }
  }
}

@Composable
private fun SongHeader(song: SongDetail) {
  val spacing = MaterialTheme.spacing
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
  ) {
    Text(
      text = song.name.value,
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      ),
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      modifier = Modifier.semantics { heading() }
    )

    val tonalities = song.tonalities
    if (!tonalities.isNullOrEmpty()) {
      Spacer(Modifier.height(spacing.md))
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = CircleShape,
      ) {
        val tonalityLabels = mutableListOf<String>()
        for (tonality in tonalities) {
          tonalityLabels.add(stringResource(tonality.label))
        }
        Text(
          text = tonalityLabels.joinToString(" • "),
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun LyricPartView(
  part: LyricPart,
  verseNumber: Int?,
  chorusIndex: Int?,
  bridgeIndex: Int?,
  isRepeat: Boolean,
  fontScale: Float,
  expandedText: Boolean,
) {
  val baseFontSize = 18.sp * fontScale
  val lineHeight = baseFontSize * 1.7f

  // When repeat chorus/bridge and option is OFF — show only a reference label
  if (isRepeat && !expandedText && part !is Verse) {
    Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
      val labelBase = when (part) {
        is Chorus -> stringResource(Res.string.song_detail_label_chorus)
        is Bridge -> stringResource(Res.string.song_detail_label_bridge)
        is Verse -> ""
      }
      val index = when (part) {
        is Chorus -> chorusIndex
        is Bridge -> bridgeIndex
        is Verse -> null
      }
      val label = if (index != null) "[$labelBase $index]" else "[$labelBase]"
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = baseFontSize * 0.85f,
          fontStyle = FontStyle.Italic
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      )
    }
    return
  }

  Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
    when (part) {
      is Verse -> {
        Row(modifier = Modifier.fillMaxWidth()) {
          if (verseNumber != null) {
            Text(
              text = verseNumber.toString(),
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = baseFontSize * 0.78f,
                fontWeight = FontWeight.SemiBold
              ),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
              modifier = Modifier.width(32.dp).padding(top = 6.dp)
            )
          }

          Text(
            text = part.text.trim(),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = baseFontSize,
              lineHeight = lineHeight,
              color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(1f),
          )
        }
      }

      is Chorus -> {
        val chorusLabel = if (chorusIndex != null) {
          "${stringResource(Res.string.song_detail_label_chorus)} $chorusIndex"
        } else {
          stringResource(Res.string.song_detail_label_chorus)
        }
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
          shape = MaterialTheme.shapes.large,
          tonalElevation = 0.dp
        ) {
          IntrinsicChorusView(
            text = part.text,
            label = chorusLabel,
            fontSize = baseFontSize,
            lineHeight = lineHeight,
            accentColor = MaterialTheme.colorScheme.primary
          )
        }
      }

      is Bridge -> {
        val bridgeLabel = if (bridgeIndex != null) {
          "${stringResource(Res.string.song_detail_label_bridge)} $bridgeIndex"
        } else {
          stringResource(Res.string.song_detail_label_bridge)
        }
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
          shape = MaterialTheme.shapes.large,
          tonalElevation = 0.dp
        ) {
          IntrinsicChorusView(
            text = part.text,
            label = bridgeLabel,
            fontSize = baseFontSize,
            lineHeight = lineHeight,
            accentColor = MaterialTheme.colorScheme.tertiary
          )
        }
      }
    }
  }
}

@Composable
private fun IntrinsicChorusView(
  text: String,
  label: String,
  fontSize: androidx.compose.ui.unit.TextUnit,
  lineHeight: androidx.compose.ui.unit.TextUnit,
  accentColor: Color
) {
  val spacing = MaterialTheme.spacing
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp, horizontal = 12.dp)
      .height(IntrinsicSize.Min)
  ) {
    Box(
      modifier = Modifier
        .width(4.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(2.dp))
        .background(accentColor)
    )
    Spacer(Modifier.width(spacing.md))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        ),
        color = accentColor
      )
      Spacer(Modifier.height(6.dp))
      Text(
        text = text.trim(),
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = fontSize,
          lineHeight = lineHeight,
          fontStyle = FontStyle.Italic
        ),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
      )
    }
  }
}

@Composable
private fun SongMetadata(song: SongDetail) {
  val bibleRefText = song.bibleRef?.toString()
  val hasBibleRef = !bibleRefText.isNullOrBlank() && bibleRefText != "-"
  val hasMetadata = song.author != null ||
    song.composer != null ||
    song.translator != null ||
    song.year != null ||
    hasBibleRef

  if (!hasMetadata) return

  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
  ) {
    Spacer(Modifier.height(spacing.xl))
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
      modifier = Modifier.padding(vertical = spacing.lg)
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
      // Info Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Outlined.Info,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(spacing.sm))
        Text(
          text = stringResource(Res.string.song_detail_info_title),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(Modifier.height(spacing.sm))

      if (song.author != null) MetadataItem(stringResource(Res.string.song_detail_info_author), song.author!!.name)
      if (song.composer != null) MetadataItem(stringResource(Res.string.song_detail_info_composer), song.composer!!.name)
      if (song.translator != null) MetadataItem(stringResource(Res.string.song_detail_info_translator), song.translator!!.name)
      if (song.year != null) MetadataItem(stringResource(Res.string.song_detail_info_year), song.year.toString())
      if (hasBibleRef) {
        MetadataItem(stringResource(Res.string.song_detail_info_bible), bibleRefText)
      }
    }
  }
}

@Composable
private fun MetadataItem(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}

@Composable
private fun SongReferencesSection(
  references: List<SongReferenceDetail>,
  referenceBookContexts: Map<io.github.alelk.pws.domain.core.ids.SongId, List<SongDetailScreenModel.ReferenceBookContextUi>>,
  currentBookId: io.github.alelk.pws.domain.core.ids.BookId?,
) {
  val navigator = LocalNavigator.currentOrThrow
  val spacing = MaterialTheme.spacing

  fun openBySongNumberId(target: SongNumberId) {
    val screen = runCatching { ScreenRegistry.get(SharedScreens.Song(target)) }
      .getOrElse { SongDetailScreen(target) }
    navigator.push(screen)
  }

  fun openBySongId(target: io.github.alelk.pws.domain.core.ids.SongId) {
    val screen = runCatching { ScreenRegistry.get(SharedScreens.SongById(target)) }
      .getOrElse { SongDetailBySongIdScreen(target) }
    navigator.push(screen)
  }

  Column(
    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
  ) {
    Spacer(Modifier.height(spacing.xl))
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
      modifier = Modifier.padding(vertical = spacing.lg)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.width(spacing.sm))
      Text(
        text = stringResource(Res.string.song_detail_see_also),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(Modifier.height(spacing.md))

    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
      references.forEach { ref ->
        val contexts = referenceBookContexts[ref.refSongId].orEmpty()
        val preferredContext = contexts.firstOrNull { it.bookId == currentBookId } ?: contexts.firstOrNull()

        SongReferenceItem(
          reference = ref,
          contexts = contexts,
          currentBookId = currentBookId,
          onContextClick = { context ->
            openBySongNumberId(context.songNumberId)
          },
          onClick = {
            preferredContext?.let {
              openBySongNumberId(it.songNumberId)
            } ?: openBySongId(ref.refSongId)
          }
        )
      }
    }
  }
}

@Composable
private fun SongReferenceItem(
  reference: SongReferenceDetail,
  contexts: List<SongDetailScreenModel.ReferenceBookContextUi>,
  currentBookId: io.github.alelk.pws.domain.core.ids.BookId?,
  onContextClick: (SongDetailScreenModel.ReferenceBookContextUi) -> Unit,
  onClick: () -> Unit
) {
  val spacing = MaterialTheme.spacing
  val orderedContexts = remember(contexts, currentBookId) {
    contexts.sortedWith(
      compareByDescending<SongDetailScreenModel.ReferenceBookContextUi> { it.bookId == currentBookId }
        .thenBy { it.bookShortTitle }
        .thenBy { it.songNumber }
    )
  }

  Card(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = reference.songName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (reference.reason == SongRefReason.Variation) {
          Text(
            text = stringResource(Res.string.song_detail_variant, reference.volume),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (contexts.isNotEmpty()) {
          Spacer(Modifier.height(6.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            orderedContexts.forEach { context ->
              val isCurrentBook = context.bookId == currentBookId
              val chipBorderColor by animateColorAsState(
                targetValue = if (isCurrentBook) {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                },
                label = "refChipBorderColor"
              )
              val chipContainerColor by animateColorAsState(
                targetValue = if (isCurrentBook) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
                } else {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                },
                label = "refChipContainerColor"
              )

              AssistChip(
                onClick = { onContextClick(context) },
                label = {
                  Text(
                    text = "${context.bookShortTitle} • № ${context.songNumber}",
                    style = MaterialTheme.typography.labelSmall
                  )
                },
                border = AssistChipDefaults.assistChipBorder(
                  enabled = true,
                  borderColor = chipBorderColor
                ),
                colors = AssistChipDefaults.assistChipColors(
                  containerColor = chipContainerColor,
                  labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              )
            }
          }
        }
      }
      Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = stringResource(Res.string.song_detail_open),
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Song Tags Section — displayed below metadata
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongTagsSection(tags: List<Tag<TagId>>) {
  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
  ) {
    Spacer(Modifier.height(spacing.xl))
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
      modifier = Modifier.padding(vertical = spacing.lg)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.AutoMirrored.Filled.Label,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.width(spacing.sm))
      Text(
        text = stringResource(Res.string.song_detail_tags),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(Modifier.height(spacing.md))
    SongTagsRow(tags = tags)
  }
}

// ---------------------------------------------------------------------------
// Song Tags Row — compact display of assigned tags
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongTagsRow(tags: List<Tag<TagId>>) {
  FlowRow(
    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    tags.forEach { tag ->
      val bgColor = Color(tag.color.r / 255f, tag.color.g / 255f, tag.color.b / 255f)
      Surface(
        shape = CircleShape,
        color = bgColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(bgColor)
          )
          Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Actions BottomSheet — Edit / Tags / Share
// ---------------------------------------------------------------------------

@Composable
private fun SongActionsSheet(
  showJump: Boolean,
  onEditSong: () -> Unit,
  onEditTags: () -> Unit,
  onJumpToNumber: () -> Unit,
  onShare: () -> Unit,
  onCopy: () -> Unit,
) {
  val spacing = MaterialTheme.spacing
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = spacing.md)
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = stringResource(Res.string.song_detail_actions),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)
    )

    ActionItem(
      icon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.testTag("action:edit-song")) },
      label = stringResource(Res.string.song_detail_action_edit),
      onClick = onEditSong
    )
    ActionItem(
      icon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.testTag("action:edit-tags")) },
      label = stringResource(Res.string.song_detail_action_edit_tags),
      onClick = onEditTags
    )
    if (showJump) {
      ActionItem(
        icon = { Icon(Icons.Filled.Search, contentDescription = null) },
        label = stringResource(Res.string.song_detail_action_jump),
        onClick = onJumpToNumber
      )
    }
    ActionItem(
      icon = { Icon(Icons.Filled.Share, contentDescription = null) },
      label = stringResource(Res.string.song_detail_action_share),
      onClick = onShare
    )
    ActionItem(
      icon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
      label = stringResource(Res.string.song_detail_action_copy),
      onClick = onCopy
    )

    Spacer(Modifier.height(spacing.md))
  }
}

@Composable
private fun ActionItem(
  icon: @Composable () -> Unit,
  label: String,
  onClick: () -> Unit,
) {
  val spacing = MaterialTheme.spacing
  Surface(
    onClick = onClick,
    color = Color.Transparent,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        icon()
      }
      Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

private fun buildShareText(song: SongDetail): String {
  val lyric = song.lyric.joinToString("\n\n") { it.text.trim() }
  return "${song.name.value}\n\n$lyric"
}

// ---------------------------------------------------------------------------
// Tag Editor BottomSheet — select / deselect tags for the song
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditorSheet(
  songTags: List<Tag<TagId>>,
  allTags: List<Tag<TagId>>,
  onSave: (Set<TagId>) -> Unit,
  onDismiss: () -> Unit,
) {
  val spacing = MaterialTheme.spacing
  val currentTagIds = remember(songTags) { songTags.map { it.id }.toSet() }
  var selected by remember(currentTagIds) { mutableStateOf(currentTagIds) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(spacing.lg)
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = stringResource(Res.string.song_detail_tags_sheet_title),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(spacing.md))

    if (allTags.isEmpty()) {
      Text(
        text = stringResource(Res.string.song_detail_tags_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    } else {
      FlowRow(
        modifier = Modifier
          .weight(1f, fill = false)
          .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
      ) {
        allTags.forEach { tag ->
          val isSelected = tag.id in selected
          val chipColor = Color(tag.color.r / 255f, tag.color.g / 255f, tag.color.b / 255f)
          FilterChip(
            selected = isSelected,
            onClick = {
              selected = if (isSelected) selected - tag.id else selected + tag.id
            },
            label = { Text(tag.name) },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(chipColor)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = chipColor.copy(alpha = 0.18f),
              selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            )
          )
        }
      }
    }

    Spacer(Modifier.height(spacing.lg))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.tags_cancel)) }
      Spacer(Modifier.width(spacing.sm))
      Button(
        onClick = { onSave(selected) },
        modifier = Modifier.testTag("action:save-song-tags")
      ) { Text(stringResource(Res.string.tags_save)) }
    }
  }
}

// ---------------------------------------------------------------------------
// Jump to Number BottomSheet
// ---------------------------------------------------------------------------

@Composable
private fun JumpToNumberSheet(
  bookNumberMap: Map<Int, SongNumberId>,
  onNavigate: (SongNumberId) -> Unit,
  onDismiss: () -> Unit,
) {
  val spacing = MaterialTheme.spacing
  val minNumber = remember(bookNumberMap) { bookNumberMap.keys.minOrNull() ?: 1 }
  val maxNumber = remember(bookNumberMap) { bookNumberMap.keys.maxOrNull() ?: 1 }

  var input by remember { mutableStateOf("") }
  var invalidInput by remember { mutableStateOf(false) }
  var missingSongNumber by remember { mutableStateOf<Int?>(null) }

  fun tryJump() {
    val number = input.trim().toIntOrNull()
    when {
      number == null -> {
        invalidInput = true
        missingSongNumber = null
      }
      number !in bookNumberMap -> {
        invalidInput = false
        missingSongNumber = number
      }
      else -> {
        invalidInput = false
        missingSongNumber = null
        onNavigate(bookNumberMap.getValue(number))
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(spacing.lg)
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    Text(
      text = stringResource(Res.string.song_detail_jump_title),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(Modifier.height(spacing.lg))

    OutlinedTextField(
      value = input,
      onValueChange = { v ->
        // allow only digits
        if (v.all { it.isDigit() }) {
          input = v
          invalidInput = false
          missingSongNumber = null
        }
      },
      label = { Text(stringResource(Res.string.song_detail_jump_number_label, minNumber, maxNumber)) },
      placeholder = { Text("$minNumber – $maxNumber", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
      isError = invalidInput || missingSongNumber != null,
      supportingText = when {
        invalidInput -> {
          {
            Text(
              stringResource(Res.string.song_detail_jump_empty),
              color = MaterialTheme.colorScheme.error
            )
          }
        }
        missingSongNumber != null -> {
          {
            Text(
              stringResource(Res.string.song_detail_jump_not_found, missingSongNumber!!),
              color = MaterialTheme.colorScheme.error
            )
          }
        }
        else -> null
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Go
      ),
      keyboardActions = KeyboardActions(
        onGo = { tryJump() }
      ),
      modifier = Modifier.fillMaxWidth(),
      trailingIcon = {
        if (input.isNotEmpty()) {
          IconButton(onClick = {
            input = ""
            invalidInput = false
            missingSongNumber = null
          }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.song_detail_jump_clear),
              modifier = Modifier.size(18.dp))
          }
        }
      }
    )

    Spacer(Modifier.height(spacing.lg))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.tags_cancel)) }
      Spacer(Modifier.width(spacing.sm))
      Button(
        onClick = ::tryJump,
        enabled = input.isNotEmpty()
      ) {
        Text(stringResource(Res.string.song_detail_action_jump))
      }
    }
  }
}
