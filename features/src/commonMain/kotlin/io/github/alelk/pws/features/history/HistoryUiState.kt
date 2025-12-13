package io.github.alelk.pws.features.history

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.core.ids.SongNumberId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Immutable
data class HistoryItemUi(
  val id: Long,
  val songNumberId: SongNumberId,
  val songNumber: Int,
  val songName: String,
  val bookDisplayName: String,
  val viewedAt: Instant
)

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

