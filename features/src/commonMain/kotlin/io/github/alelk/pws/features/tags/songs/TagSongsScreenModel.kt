package io.github.alelk.pws.features.tags.songs

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.TagId
import kotlinx.coroutines.launch

/**
 * ScreenModel for tag songs screen.
 */
class TagSongsScreenModel(
  private val tagId: TagId
  // TODO: inject use cases
  // private val getTagUseCase: GetTagUseCase,
  // private val getSongsByTagUseCase: GetSongsByTagUseCase
) : StateScreenModel<TagSongsUiState>(TagSongsUiState.Loading) {

  init {
    loadTagSongs()
  }

  private fun loadTagSongs() {
    screenModelScope.launch {
      try {
        // TODO: Replace with actual implementation
        // val tag = getTagUseCase(tagId)
        // val songs = getSongsByTagUseCase(tagId)

        // Placeholder
        mutableState.value = TagSongsUiState.Empty
      } catch (e: Exception) {
        mutableState.value = TagSongsUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }
}

