package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationClickedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationPromptDismissedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase
import io.github.alelk.pws.features.di.DonationSessionGuard
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * ScreenModel for SongDetailBySongIdScreen.
 * Provides same functionality as SongDetailScreenModel but for a song identified by SongId.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailBySongIdScreenModel(  val songId: SongId,
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

  private val _isFavorite = MutableStateFlow(false)
  val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()


  private val _references = MutableStateFlow<List<SongReferenceDetail>>(emptyList())
  val references: StateFlow<List<SongReferenceDetail>> = _references.asStateFlow()

  private val _referenceBookContexts = MutableStateFlow<Map<SongId, List<SongDetailScreenModel.ReferenceBookContextUi>>>(emptyMap())
  val referenceBookContexts: StateFlow<Map<SongId, List<SongDetailScreenModel.ReferenceBookContextUi>>> = _referenceBookContexts.asStateFlow()

  private val _songTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val songTags: StateFlow<List<Tag<TagId>>> = _songTags.asStateFlow()

  private val _allTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val allTags: StateFlow<List<Tag<TagId>>> = _allTags.asStateFlow()

  private val booksSnapshot = MutableStateFlow<List<BookSummary>>(emptyList())
  private val _currentSongNumberId = MutableStateFlow<SongNumberId?>(null)

  /** Effective coroutine scope: injected for tests, otherwise Voyager's screenModelScope. */
  private val scope: CoroutineScope get() = coroutineScope ?: screenModelScope

  init {
    scope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      observeSong(songId).collectLatest { detail: SongDetail? ->
        mutableState.value = detail?.let {
          SongDetailUiState.Content(song = it, context = SongDetailUiState.DisplayContext())
        } ?: SongDetailUiState.Error
        refreshMainSongContext()
      }
    }

    scope.launch {
      observeBooks().collectLatest { books ->
        booksSnapshot.value = books
        refreshMainSongContext()
        refreshReferenceContexts()
      }
    }

    scope.launch {
      observeIsFavorite(FavoriteSubject.StandaloneSong(songId)).collectLatest { isFav ->
        _isFavorite.value = isFav
      }
    }

    scope.launch {
      try {
        _references.value = getSongReferences(songId).fold(
          ifLeft = { emptyList() },
          ifRight = { it }
        )
        refreshReferenceContexts()
      } catch (_: Exception) {}
    }

    scope.launch {
      _currentSongNumberId.collectLatest { id ->
        if (id == null) {
          _songTags.value = emptyList()
          return@collectLatest
        }
        observeTagsForSong(id).collectLatest { tags ->
          _songTags.value = tags
        }
      }
    }

    scope.launch {
      observeAllTags().collectLatest { tags ->
        _allTags.value = tags
      }
    }

    // Auto-record song view after viewDelay once the song content is loaded.
    scope.launch {
      state.first { it is SongDetailUiState.Content }
      kotlinx.coroutines.delay(viewDelay)
      try {
        recordSongView(HistorySubject.StandaloneSong(songId))
        checkAndShowDonationPrompt()
      } catch (_: Exception) {}
    }
  }

  fun onToggleFavorite() {
    scope.launch {
      try {
        toggleFavorite(FavoriteSubject.StandaloneSong(songId))
      } catch (_: Exception) {}
    }
  }

  fun onSaveTags(selectedTagIds: Set<TagId>) {
    scope.launch {
      try {
        replaceAllSongTags(songId, selectedTagIds)
      } catch (_: Exception) {}
    }
  }

  /** Called when the user taps "Later" (×) on the donation prompt card.
   * Suppresses for [shortSuppressViews] views and blocks the card for the rest of this session. */
  fun onDonationDismissed() {
    scope.launch {
      try {
        recordDonationDismissed()
        donationSessionGuard.shownThisSession = true   // suppress for rest of session
        val current = mutableState.value as? SongDetailUiState.Content ?: return@launch
        mutableState.value = current.copy(showDonationCard = false)
      } catch (_: Exception) {}
    }
  }

  /** Called when the user taps "Support on Boosty". Permanently suppresses the prompt. */
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
        // After seeing the card maxShowsPerSession times without any action,
        // suppress for the rest of this session (no persistent suppress in SharedPreferences).
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

  private suspend fun refreshMainSongContext() {
    val current = mutableState.value as? SongDetailUiState.Content ?: return
    val activeBooks = booksSnapshot.value.filter { it.enabled }.associateBy { it.id }
    if (activeBooks.isEmpty()) return

    val songNumbers = songNumberReadRepository.getAllBySongId(songId)
    val bestSn = songNumbers
      .filter { it.bookId in activeBooks }
      .maxByOrNull { activeBooks[it.bookId]?.priority ?: 0 }

    if (bestSn != null) {
      val book = activeBooks[bestSn.bookId]!!
      val snId = SongNumberId(book.id, songId)
      _currentSongNumberId.value = snId
      mutableState.value = current.copy(
        context = SongDetailUiState.DisplayContext(
          songNumber = bestSn.number,
          bookTitle = book.displayName.value
        )
      )
    }
  }

  private suspend fun refreshReferenceContexts() {
    val references = _references.value
    val targetSongIds = references.map { it.refSongId }.toSet()
    if (targetSongIds.isEmpty()) {
      _referenceBookContexts.value = emptyMap()
      return
    }

    val bySong = linkedMapOf<SongId, MutableList<SongDetailScreenModel.ReferenceBookContextUi>>()
    val activeBooks = booksSnapshot.value.filter { it.enabled }.associateBy { it.id }

    targetSongIds.forEach { targetId ->
      val songNumbers = songNumberReadRepository.getAllBySongId(targetId)
      songNumbers.forEach { sn ->
        val book = activeBooks[sn.bookId] ?: return@forEach
        bySong.getOrPut(targetId) { mutableListOf() }
          .add(
            SongDetailScreenModel.ReferenceBookContextUi(
              songNumberId = SongNumberId(book.id, targetId),
              bookId = book.id,
              bookTitle = book.displayName.value,
              bookShortTitle = book.displayShortName.value,
              songNumber = sn.number,
            )
          )
      }
    }

    val bookPriority = activeBooks.mapValues { it.value.priority }

    _referenceBookContexts.value = bySong.mapValues { (_, list) ->
      list.distinctBy { it.songNumberId.identifier }
        .sortedWith(
          compareByDescending<SongDetailScreenModel.ReferenceBookContextUi> { bookPriority[it.bookId] ?: 0 }
            .thenBy { it.bookShortTitle }
            .thenBy { it.songNumber }
        )
    }
  }
}
