package io.github.alelk.pws.features.history

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.history.model.HistoryEntryWithSongInfo
import io.github.alelk.pws.domain.history.usecase.ClearHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.ObserveHistoryUseCase
import io.github.alelk.pws.domain.history.usecase.RemoveHistoryEntryUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * ScreenModel for history screen.
 */
class HistoryScreenModel(
  private val observeHistoryUseCase: ObserveHistoryUseCase,
  private val removeHistoryItemUseCase: RemoveHistoryEntryUseCase,
  private val clearHistoryUseCase: ClearHistoryUseCase
) : StateScreenModel<HistoryUiState>(HistoryUiState.Loading) {

  sealed interface Effect {
    data class NavigateToSong(val item: HistoryItemUi) : Effect
  }

  private val _effects = MutableSharedFlow<Effect>()
  val effects = _effects.asSharedFlow()

  private val _showClearDialog = MutableStateFlow(false)
  val showClearDialog = _showClearDialog.asStateFlow()

  init {
    loadHistory()
  }

  fun onEvent(event: HistoryEvent) {
    when (event) {
      is HistoryEvent.ItemClicked -> {
        screenModelScope.launch {
          _effects.emit(Effect.NavigateToSong(event.item))
        }
      }

      is HistoryEvent.RemoveItem -> {
        removeItem(event.item)
      }

      HistoryEvent.ClearAll -> {
        _showClearDialog.value = true
      }

      HistoryEvent.ConfirmClearAll -> {
        _showClearDialog.value = false
        clearAll()
      }

      HistoryEvent.DismissClearDialog -> {
        _showClearDialog.value = false
      }
    }
  }

  private fun loadHistory() {
    screenModelScope.launch {
      try {
        observeHistoryUseCase().collect { items ->
          val uiItems = items.map { it.toUi() }
          mutableState.value = if (uiItems.isEmpty()) {
            HistoryUiState.Empty
          } else {
            HistoryUiState.Content(uiItems)
          }
        }
      } catch (e: Exception) {
        mutableState.value = HistoryUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun removeItem(item: HistoryItemUi) {
    screenModelScope.launch {
      try {
        removeHistoryItemUseCase(item.songNumberId)
      } catch (e: Exception) {
        // Handle error
      }
    }
  }

  private fun clearAll() {
    screenModelScope.launch {
      try {
        clearHistoryUseCase()
        mutableState.value = HistoryUiState.Empty
      } catch (e: Exception) {
        // Handle error
      }
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun HistoryEntryWithSongInfo.toUi() = HistoryItemUi(
    id = id,
    songNumberId = songNumberId,
    songNumber = songNumber,
    songName = songName,
    bookDisplayName = bookDisplayName,
    viewedAt = viewedAt
  )
}

