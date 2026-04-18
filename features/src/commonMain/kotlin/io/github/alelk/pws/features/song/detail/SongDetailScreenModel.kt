package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import io.github.alelk.pws.domain.songreference.usecase.SongReferenceDetail
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailScreenModel(
  val songNumberId: SongNumberId,
  private val observeSong: ObserveSongUseCase,
  private val songObserveRepository: SongObserveRepository,
  private val recordSongView: RecordSongViewUseCase,
  private val observeIsFavorite: ObserveIsFavoriteUseCase,
  private val toggleFavorite: ToggleFavoriteUseCase,
  private val getSongReferences: GetSongReferencesWithDetailsUseCase,
  private val observeTagsForSong: ObserveTagsForSongUseCase<TagId>,
  private val observeAllTags: ObserveTagsUseCase<TagId>,
  private val replaceAllSongTags: ReplaceAllSongTagsUseCase<TagId>,
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  /** Currently active song number id — changes when user swipes. */
  private val _currentSongNumberId = MutableStateFlow(songNumberId)
  val currentSongNumberId: StateFlow<SongNumberId> = _currentSongNumberId.asStateFlow()

  private val _isFavorite = MutableStateFlow(false)
  val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

  /** Ordered list of SongNumberIds in the same book (sorted by number). Empty until loaded. */
  private val _bookSongNumberIds = MutableStateFlow<List<SongNumberId>>(emptyList())
  val bookSongNumberIds: StateFlow<List<SongNumberId>> = _bookSongNumberIds.asStateFlow()

  /** Map: song number (Int) → SongNumberId. Used for "jump by number" navigation. */
  private val _bookNumberMap = MutableStateFlow<Map<Int, SongNumberId>>(emptyMap())
  val bookNumberMap: StateFlow<Map<Int, SongNumberId>> = _bookNumberMap.asStateFlow()

  /** Cross-references to related songs. Empty until loaded. */
  private val _references = MutableStateFlow<List<SongReferenceDetail>>(emptyList())
  val references: StateFlow<List<SongReferenceDetail>> = _references.asStateFlow()

  /** Tags assigned to the current song. */
  private val _songTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val songTags: StateFlow<List<Tag<TagId>>> = _songTags.asStateFlow()

  /** All available tags (for the tag editor). */
  private val _allTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val allTags: StateFlow<List<Tag<TagId>>> = _allTags.asStateFlow()

  init {
    // Observe currently displayed song content
    screenModelScope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      _currentSongNumberId.flatMapLatest { id ->
        mutableState.value = SongDetailUiState.Loading
        observeSong(id.songId)
      }.collectLatest { detail: SongDetail? ->
        mutableState.value = detail?.let { SongDetailUiState.Content(it) } ?: SongDetailUiState.Error
      }
    }

    // Observe favorite status, reacting to song changes
    screenModelScope.launch {
      _currentSongNumberId.flatMapLatest { id ->
        observeIsFavorite(FavoriteSubject.BookedSong(id))
      }.collectLatest { isFav ->
        _isFavorite.value = isFav
      }
    }

    // Load all song IDs in the same book for swipe navigation and jump-by-number
    screenModelScope.launch {
      try {
        songObserveRepository.observeAllInBook(songNumberId.bookId).collectLatest { songsMap ->
          // songsMap: Map<Int /* number */, SongSummary>
          val sorted = songsMap.entries.sortedBy { it.key }
          _bookSongNumberIds.value = sorted.map { (_, summary) ->
            SongNumberId(songNumberId.bookId, summary.id)
          }
          _bookNumberMap.value = sorted.associate { (number, summary) ->
            number to SongNumberId(songNumberId.bookId, summary.id)
          }
        }
      } catch (_: Exception) {
        // Navigation without swipe list is acceptable
      }
    }

    // Load cross-references for the current song
    screenModelScope.launch {
      _currentSongNumberId.collectLatest { currentId ->
        try {
          _references.value = getSongReferences(currentId.songId)
        } catch (_: Exception) {
          _references.value = emptyList()
        }
      }
    }

    // Observe tags for the current song
    screenModelScope.launch {
      _currentSongNumberId.flatMapLatest { id ->
        observeTagsForSong(id)
      }.collectLatest { tags ->
        _songTags.value = tags
      }
    }

    // Observe all available tags (for tag editor)
    screenModelScope.launch {
      observeAllTags().collectLatest { tags ->
        _allTags.value = tags
      }
    }
  }

  /** Called by the pager when the user swipes to a different song. */
  fun onPageChanged(newSongNumberId: SongNumberId) {
    if (_currentSongNumberId.value != newSongNumberId) {
      _currentSongNumberId.value = newSongNumberId
    }
  }

  fun onSongViewed() {
    screenModelScope.launch {
      try {
        recordSongView(HistorySubject.BookedSong(_currentSongNumberId.value))
      } catch (_: Exception) {}
    }
  }

  fun onToggleFavorite() {
    screenModelScope.launch {
      try {
        toggleFavorite(FavoriteSubject.BookedSong(_currentSongNumberId.value))
      } catch (_: Exception) {}
    }
  }

  /** Save the selected set of tags for the current song. */
  fun onSaveTags(selectedTagIds: Set<TagId>) {
    screenModelScope.launch {
      try {
        replaceAllSongTags(_currentSongNumberId.value.songId, selectedTagIds)
      } catch (_: Exception) {}
    }
  }

  /**
   * Returns the [SongNumberId] for the given song number in the current book,
   * or null if no song with that number exists.
   */
  fun resolveNumber(number: Int): SongNumberId? = _bookNumberMap.value[number]
}
