package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationClickedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationPromptDismissedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import io.github.alelk.pws.domain.songreference.usecase.SongReferenceDetail
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.features.app.UiMessage
import io.github.alelk.pws.features.di.DonationSessionGuard
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailScreenModel(
  val songNumberId: SongNumberId,
  private val observeSong: ObserveSongUseCase,
  private val observeBooks: ObserveBooksUseCase,
  private val songObserveRepository: SongObserveRepository,
  private val recordSongView: RecordSongViewUseCase,
  private val observeIsFavorite: ObserveIsFavoriteUseCase,
  private val toggleFavorite: ToggleFavoriteUseCase,
  private val getSongReferences: GetSongReferencesWithDetailsUseCase,
  private val songNumberReadRepository: SongNumberReadRepository,
  private val observeTagsForSong: ObserveTagsForSongUseCase<TagId>,
  private val observeAllTags: ObserveTagsUseCase<TagId>,
  private val replaceAllSongTags: ReplaceAllSongTagsUseCase<TagId>,
  private val shouldShowDonationPrompt: ShouldShowDonationPromptUseCase,
  private val recordDonationDismissed: RecordDonationPromptDismissedUseCase,
  private val recordDonationClicked: RecordDonationClickedUseCase,
  private val donationConfig: DonationConfig,
  private val donationSessionGuard: DonationSessionGuard,
  /** Injected scope for testing with virtual time; null = use [screenModelScope]. */
  private val coroutineScope: CoroutineScope? = null,
  /** Delay before recording a song view. Override in tests to speed up. */
  internal val viewDelay: Duration = 5.seconds,
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  data class ReferenceBookContextUi(
    val songNumberId: SongNumberId,
    val bookId: BookId,
    val bookTitle: String,
    val bookShortTitle: String,
    val songNumber: Int,
  )

  // ---------------------------------------------------------------------------
  // Pager / jump-by-number — book-level, independent of which song is shown.
  // ---------------------------------------------------------------------------

  private val _currentSongNumberId = MutableStateFlow(songNumberId)
  val currentSongNumberId: StateFlow<SongNumberId> = _currentSongNumberId.asStateFlow()

  private val _bookSongNumberIds = MutableStateFlow<List<SongNumberId>>(emptyList())
  val bookSongNumberIds: StateFlow<List<SongNumberId>> = _bookSongNumberIds.asStateFlow()

  private val _bookNumberMap = MutableStateFlow<Map<Int, SongNumberId>>(emptyMap())
  val bookNumberMap: StateFlow<Map<Int, SongNumberId>> = _bookNumberMap.asStateFlow()

  /** Reverse index: songId → song number inside the current book. */
  private val songIdToNumber = MutableStateFlow<Map<SongId, Int>>(emptyMap())
  private val bookTitle = MutableStateFlow<String?>(null)
  private val booksSnapshot = MutableStateFlow<List<BookSummary>>(emptyList())

  /** All available tags (for the tag editor sheet) — global, not per-song. */
  private val _allTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val allTags: StateFlow<List<Tag<TagId>>> = _allTags.asStateFlow()

  // ---------------------------------------------------------------------------
  // Effects
  // ---------------------------------------------------------------------------

  sealed interface Effect {
    data class ShowError(val message: UiMessage) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  private val scope: CoroutineScope get() = coroutineScope ?: screenModelScope

  /** Cached per-song bits not in [observeSong]. Folded into Content alongside the song. */
  private data class PerSongExtras(
    val isFavorite: Boolean,
    val songTags: List<Tag<TagId>>,
    val references: List<SongReferenceDetail>,
    val referenceBookContexts: Map<SongId, List<ReferenceBookContextUi>>,
  )

  init {
    // Per-song flow: emit Loading whenever the active song changes; on the new
    // song id, observe song + favourite + tags + references and fold them into
    // a single Content (or Error if the song is missing).
    scope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      _currentSongNumberId
        .onEach { mutableState.value = SongDetailUiState.Loading }
        .flatMapLatest { id ->
          val refs = loadReferences(id.songId)
          combineLatest4(
            observeSong(id.songId),
            observeIsFavorite(FavoriteSubject.BookedSong(id)),
            observeTagsForSong(id),
            booksSnapshot,
          ) { song, isFav, tags, books ->
            song to PerSongExtras(
              isFavorite = isFav,
              songTags = tags,
              references = refs,
              referenceBookContexts = computeReferenceBookContexts(refs, books),
            )
          }
        }
        .collectLatest { (detail, extras) ->
          mutableState.value = detail?.let {
            SongDetailUiState.Content(
              song = it,
              context = SongDetailUiState.DisplayContext(
                songNumber = songIdToNumber.value[it.id],
                bookTitle = bookTitle.value,
              ),
              isFavorite = extras.isFavorite,
              songTags = extras.songTags,
              references = extras.references,
              referenceBookContexts = extras.referenceBookContexts,
            )
          } ?: SongDetailUiState.Error
        }
    }

    // Book context — title and song list for the pager. Book-level, not per-song.
    scope.launch {
      observeBooks().collectLatest { books ->
        booksSnapshot.value = books
        bookTitle.value = books.firstOrNull { it.id == songNumberId.bookId }?.displayName?.value
        refreshContextIfContent()
      }
    }

    scope.launch {
      try {
        songObserveRepository.observeAllInBook(songNumberId.bookId).collectLatest { songsMap ->
          val sorted = songsMap.entries.sortedBy { it.key }
          songIdToNumber.value = sorted.associate { (number, summary) -> summary.id to number }
          _bookSongNumberIds.value = sorted.map { (_, summary) -> SongNumberId(songNumberId.bookId, summary.id) }
          _bookNumberMap.value = sorted.associate { (number, summary) -> number to SongNumberId(songNumberId.bookId, summary.id) }
          refreshContextIfContent()
        }
      } catch (_: Exception) {
        // Navigation without swipe list is acceptable
      }
    }

    scope.launch {
      observeAllTags().collectLatest { _allTags.value = it }
    }

    // Auto-record song view after viewDelay once Content for the current song is loaded.
    scope.launch {
      _currentSongNumberId.collectLatest { id ->
        state.first { s -> s is SongDetailUiState.Content && s.song.id == id.songId }
        kotlinx.coroutines.delay(viewDelay)
        try {
          recordSongView(HistorySubject.BookedSong(id))
          checkAndShowDonationPrompt()
        } catch (_: Exception) {}
      }
    }
  }

  /** Called by the pager when the user swipes to a different song. */
  fun onPageChanged(newSongNumberId: SongNumberId) {
    if (_currentSongNumberId.value != newSongNumberId) {
      _currentSongNumberId.value = newSongNumberId
    }
  }

  fun onToggleFavorite() {
    scope.launch {
      try {
        toggleFavorite(FavoriteSubject.BookedSong(_currentSongNumberId.value))
      } catch (e: Exception) {
        _effects.emit(Effect.ShowError(UiMessage.Failure(e.message)))
      }
    }
  }

  fun onSaveTags(selectedTagIds: Set<TagId>) {
    scope.launch {
      replaceAllSongTags(_currentSongNumberId.value.songId, selectedTagIds).fold(
        ifLeft = { error -> _effects.emit(Effect.ShowError(UiMessage.Failure(error.message))) },
        ifRight = { },
      )
    }
  }

  fun resolveNumber(number: Int): SongNumberId? = _bookNumberMap.value[number]

  fun onDonationDismissed() {
    scope.launch {
      try {
        recordDonationDismissed()
        donationSessionGuard.shownThisSession = true
        val current = mutableState.value as? SongDetailUiState.Content ?: return@launch
        mutableState.value = current.copy(showDonationCard = false)
      } catch (_: Exception) {}
    }
  }

  fun onDonationClicked() {
    scope.launch {
      try {
        recordDonationClicked()
        val current = mutableState.value as? SongDetailUiState.Content ?: return@launch
        mutableState.value = current.copy(showDonationCard = false)
      } catch (_: Exception) {}
    }
  }

  private suspend fun checkAndShowDonationPrompt() {
    try {
      if (shouldShowDonationPrompt()) {
        donationSessionGuard.seenCountThisSession++
        if (donationSessionGuard.seenCountThisSession >= donationConfig.maxShowsPerSession) {
          donationSessionGuard.shownThisSession = true
        }
        val current = mutableState.value as? SongDetailUiState.Content ?: return
        mutableState.value = current.copy(
          showDonationCard = true,
          donationBoostyUrl = donationConfig.boostyUrl,
        )
      }
    } catch (_: Exception) {}
  }

  private fun refreshContextIfContent() {
    val current = mutableState.value as? SongDetailUiState.Content ?: return
    mutableState.value = current.copy(
      context = SongDetailUiState.DisplayContext(
        songNumber = songIdToNumber.value[current.song.id],
        bookTitle = bookTitle.value,
      ),
    )
  }

  private suspend fun loadReferences(songId: SongId): List<SongReferenceDetail> =
    try {
      getSongReferences(songId).fold(ifLeft = { emptyList() }, ifRight = { it })
    } catch (_: Exception) {
      emptyList()
    }

  private suspend fun computeReferenceBookContexts(
    references: List<SongReferenceDetail>,
    books: List<BookSummary>,
  ): Map<SongId, List<ReferenceBookContextUi>> {
    val targetSongIds = references.map { it.refSongId }.toSet()
    if (targetSongIds.isEmpty()) return emptyMap()

    val activeBooks = books.filter { it.enabled }.associateBy { it.id }
    val bookPriority = activeBooks.mapValues { it.value.priority }

    val bySong = linkedMapOf<SongId, MutableList<ReferenceBookContextUi>>()
    targetSongIds.forEach { songId ->
      val songNumbers = songNumberReadRepository.getAllBySongId(songId)
      songNumbers.forEach { sn ->
        val book = activeBooks[sn.bookId] ?: return@forEach
        bySong.getOrPut(songId) { mutableListOf() }
          .add(
            ReferenceBookContextUi(
              songNumberId = SongNumberId(book.id, songId),
              bookId = book.id,
              bookTitle = book.displayName.value,
              bookShortTitle = book.displayShortName.value,
              songNumber = sn.number,
            )
          )
      }
    }

    return bySong.mapValues { (_, list) ->
      list.distinctBy { it.songNumberId.identifier }
        .sortedWith(
          compareByDescending<ReferenceBookContextUi> { bookPriority[it.bookId] ?: 0 }
            .thenBy { it.bookShortTitle }
            .thenBy { it.songNumber }
        )
    }
  }
}

/** kotlinx-coroutines lacks a varargs combine with 4 args; this is the shim. */
private fun <T1, T2, T3, T4, R> combineLatest4(
  f1: kotlinx.coroutines.flow.Flow<T1>,
  f2: kotlinx.coroutines.flow.Flow<T2>,
  f3: kotlinx.coroutines.flow.Flow<T3>,
  f4: kotlinx.coroutines.flow.Flow<T4>,
  transform: suspend (T1, T2, T3, T4) -> R,
): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(f1, f2, f3, f4, transform)
