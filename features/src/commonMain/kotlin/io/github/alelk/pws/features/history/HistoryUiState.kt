package io.github.alelk.pws.features.history

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.history.model.HistorySubject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * UI representation of a history entry.
 * Supports both booked songs (in a book) and standalone songs.
 */
@OptIn(ExperimentalTime::class)
@Immutable
sealed interface HistoryItemUi {
  val id: Long
  val subject: HistorySubject
  val songName: String
  val viewedAt: Instant
  val viewCount: Int

  /**
   * A song viewed in context of a book.
   */
  @Immutable
  data class BookedSong(
    override val id: Long,
    override val subject: HistorySubject.BookedSong,
    val songNumber: Int,
    override val songName: String,
    val bookDisplayName: String,
    override val viewedAt: Instant,
    override val viewCount: Int
  ) : HistoryItemUi

  /**
   * A standalone song viewed without book context.
   */
  @Immutable
  data class StandaloneSong(
    override val id: Long,
    override val subject: HistorySubject.StandaloneSong,
    override val songName: String,
    override val viewedAt: Instant,
    override val viewCount: Int
  ) : HistoryItemUi
}

sealed interface HistoryUiState {
  data object Loading : HistoryUiState

  @Immutable
  data class Content(
    val items: List<HistoryItemUi>,
    val canClearAll: Boolean = true
  ) : HistoryUiState

  data object Empty : HistoryUiState
  data class Error(val message: String) : HistoryUiState
}

sealed interface HistoryEvent {
  data class ItemClicked(val item: HistoryItemUi) : HistoryEvent
  data class RemoveItem(val item: HistoryItemUi) : HistoryEvent
  data object ClearAll : HistoryEvent
  data object ConfirmClearAll : HistoryEvent
  data object DismissClearDialog : HistoryEvent
}

