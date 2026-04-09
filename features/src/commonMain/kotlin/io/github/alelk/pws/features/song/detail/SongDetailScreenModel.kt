package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.SongNumberId
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
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  /** Currently active song number id — changes when user swipes. */
  private val _currentSongNumberId = MutableStateFlow(songNumberId)
  val currentSongNumberId: StateFlow<SongNumberId> = _currentSongNumberId.asStateFlow()

  private val _isFavorite = MutableStateFlow(false)
  val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

  /** Ordered list of SongNumberIds in the same book (sorted by number). Empty until loaded. */
  private val _bookSongNumberIds = MutableStateFlow<List<SongNumberId>>(emptyList())
  val bookSongNumberIds: StateFlow<List<SongNumberId>> = _bookSongNumberIds.asStateFlow()

  /** Cross-references to related songs. Empty until loaded. */
  private val _references = MutableStateFlow<List<SongReferenceDetail>>(emptyList())
  val references: StateFlow<List<SongReferenceDetail>> = _references.asStateFlow()

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

    // Load all song IDs in the same book for swipe navigation
    screenModelScope.launch {
      try {
        songObserveRepository.observeAllInBook(songNumberId.bookId).collectLatest { songsMap ->
          // songsMap: Map<Int /* number */, SongSummary>
          _bookSongNumberIds.value = songsMap.entries
            .sortedBy { it.key }
            .map { (_, summary) -> SongNumberId(songNumberId.bookId, summary.id) }
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
      } catch (_: Exception) {
        // Ignore errors when recording view
      }
    }
  }

  fun onToggleFavorite() {
    screenModelScope.launch {
      try {
        toggleFavorite(FavoriteSubject.BookedSong(_currentSongNumberId.value))
      } catch (_: Exception) {
        // Handle error if needed
      }
    }
  }
}
