package io.github.alelk.pws.features.song.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
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
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * ScreenModel for SongDetailBySongIdScreen.
 * Same per-song [SongDetailUiState.Content] shape as [SongDetailScreenModel] but for
 * a song identified by [SongId] (when navigating from a search result with no book).
 *
 * The resolved [SongNumberId] (best book for this song) is computed once books load
 * and feeds the tag observation — until then `songTags` stays empty.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailBySongIdScreenModel(
  val songId: SongId,
  private val observeSong: ObserveSongUseCase,
  private val observeBooks: ObserveBooksUseCase,
  @Suppress("UNUSED_PARAMETER") songObserveRepository: SongObserveRepository,
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
  private val coroutineScope: CoroutineScope? = null,
  internal val viewDelay: Duration = 5.seconds,
) : StateScreenModel<SongDetailUiState>(SongDetailUiState.Loading) {

  /** All available tags (for the tag editor sheet) — global, not per-song. */
  private val _allTags = MutableStateFlow<List<Tag<TagId>>>(emptyList())
  val allTags: StateFlow<List<Tag<TagId>>> = _allTags.asStateFlow()

  /** Resolved SongNumberId (best book for this song) once books load; null otherwise. */
  private val resolvedSongNumberId = MutableStateFlow<SongNumberId?>(null)

  sealed interface Effect {
    data class ShowError(val message: UiMessage) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  private val scope: CoroutineScope get() = coroutineScope ?: screenModelScope

  init {
    // Observe song + isFavourite; emit Content on each tick. Book context, tags
    // and reference contexts are layered in via separate observers below.
    scope.launch(context = CoroutineExceptionHandler { _, _ -> mutableState.value = SongDetailUiState.Error }) {
      val refs = try {
        getSongReferences(songId).fold(ifLeft = { emptyList() }, ifRight = { it })
      } catch (_: Exception) {
        emptyList<SongReferenceDetail>()
      }

      kotlinx.coroutines.flow.combine(
        observeSong(songId),
        observeIsFavorite(FavoriteSubject.StandaloneSong(songId)),
      ) { song, isFav -> song to isFav }
        .collectLatest { (song: SongDetail?, isFav) ->
          mutableState.value = song?.let {
            val current = mutableState.value as? SongDetailUiState.Content
            SongDetailUiState.Content(
              song = it,
              context = current?.context ?: SongDetailUiState.DisplayContext(),
              isFavorite = isFav,
              songTags = current?.songTags.orEmpty(),
              references = refs,
              referenceBookContexts = current?.referenceBookContexts.orEmpty(),
              showDonationCard = current?.showDonationCard ?: false,
              donationBoostyUrl = current?.donationBoostyUrl ?: "",
            )
          } ?: SongDetailUiState.Error
        }
    }

    // Books — resolve display context (best book) and reference contexts.
    scope.launch {
      observeBooks().collectLatest { books ->
        val activeBooks = books.filter { it.enabled }.associateBy { it.id }
        if (activeBooks.isNotEmpty()) {
          val songNumbers = songNumberReadRepository.getAllBySongId(songId)
          val bestSn = songNumbers
            .filter { it.bookId in activeBooks }
            .maxByOrNull { activeBooks[it.bookId]?.priority ?: 0 }
          if (bestSn != null) {
            val book = activeBooks[bestSn.bookId]!!
            resolvedSongNumberId.value = SongNumberId(book.id, songId)
            updateContent { content ->
              content.copy(
                context = SongDetailUiState.DisplayContext(
                  songNumber = bestSn.number,
                  bookTitle = book.displayName.value,
                ),
              )
            }
          }
        }

        // Recompute reference book contexts on each books emission.
        val current = mutableState.value as? SongDetailUiState.Content ?: return@collectLatest
        val refContexts = computeReferenceBookContexts(current.references, books)
        updateContent { it.copy(referenceBookContexts = refContexts) }
      }
    }

    // Tags — gated on resolved SongNumberId.
    scope.launch {
      resolvedSongNumberId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else observeTagsForSong(id) }
        .collectLatest { tags -> updateContent { it.copy(songTags = tags) } }
    }

    scope.launch { observeAllTags().collectLatest { _allTags.value = it } }

    // Auto-record song view after viewDelay once Content is loaded.
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
      } catch (e: Exception) {
        _effects.emit(Effect.ShowError(UiMessage.Failure(e.message)))
      }
    }
  }

  fun onSaveTags(selectedTagIds: Set<TagId>) {
    scope.launch {
      try {
        replaceAllSongTags(songId, selectedTagIds)
      } catch (e: Exception) {
        _effects.emit(Effect.ShowError(UiMessage.Failure(e.message)))
      }
    }
  }

  fun onDonationDismissed() {
    scope.launch {
      try {
        recordDonationDismissed()
        donationSessionGuard.shownThisSession = true
        updateContent { it.copy(showDonationCard = false) }
      } catch (_: Exception) {}
    }
  }

  fun onDonationClicked() {
    scope.launch {
      try {
        recordDonationClicked()
        updateContent { it.copy(showDonationCard = false) }
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
        updateContent { it.copy(showDonationCard = true, donationBoostyUrl = donationConfig.boostyUrl) }
      }
    } catch (_: Exception) {}
  }

  private inline fun updateContent(transform: (SongDetailUiState.Content) -> SongDetailUiState.Content) {
    val current = mutableState.value as? SongDetailUiState.Content ?: return
    mutableState.value = transform(current)
  }

  private suspend fun computeReferenceBookContexts(
    references: List<SongReferenceDetail>,
    books: List<BookSummary>,
  ): Map<SongId, List<SongDetailScreenModel.ReferenceBookContextUi>> {
    val targetSongIds = references.map { it.refSongId }.toSet()
    if (targetSongIds.isEmpty()) return emptyMap()

    val activeBooks = books.filter { it.enabled }.associateBy { it.id }
    val bookPriority = activeBooks.mapValues { it.value.priority }

    val bySong = linkedMapOf<SongId, MutableList<SongDetailScreenModel.ReferenceBookContextUi>>()
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

    return bySong.mapValues { (_, list) ->
      list.distinctBy { it.songNumberId.identifier }
        .sortedWith(
          compareByDescending<SongDetailScreenModel.ReferenceBookContextUi> { bookPriority[it.bookId] ?: 0 }
            .thenBy { it.bookShortTitle }
            .thenBy { it.songNumber }
        )
    }
  }
}
