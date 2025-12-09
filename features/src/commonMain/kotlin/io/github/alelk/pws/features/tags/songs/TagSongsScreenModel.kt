package io.github.alelk.pws.features.tags.songs

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.songtag.usecase.ObserveSongsByTagUseCase
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.usecase.GetTagDetailUseCase
import kotlinx.coroutines.launch
import io.github.alelk.pws.domain.core.Color as DomainColor

/**
 * ScreenModel for tag songs screen.
 */
class TagSongsScreenModel(
  private val tagId: TagId,
  private val getTagDetailUseCase: GetTagDetailUseCase,
  private val observeSongsByTagUseCase: ObserveSongsByTagUseCase
) : StateScreenModel<TagSongsUiState>(TagSongsUiState.Loading) {

  init {
    loadTagSongs()
  }

  private fun loadTagSongs() {
    screenModelScope.launch {
      try {
        val tag = getTagDetailUseCase(tagId)
        if (tag == null) {
          mutableState.value = TagSongsUiState.Error("Тег не найден")
          return@launch
        }
        observeSongsByTagUseCase(tagId).collect { songs ->
          mutableState.value = if (songs.isEmpty()) {
            TagSongsUiState.Empty
          } else {
            TagSongsUiState.Content(tag.toUi(), songs.map { it.toUi() })
          }
        }
      } catch (e: Exception) {
        mutableState.value = TagSongsUiState.Error("Ошибка загрузки: ${e.message}")
      }
    }
  }

  private fun TagDetail.toUi() = TagInfoUi(
    id = id,
    name = name,
    color = color.toCompose()
  )

  private fun SongWithBookInfo.toUi() = TagSongUi(
    songNumberId = songNumberId,
    songNumber = songNumber,
    songName = songName,
    bookDisplayName = bookDisplayName
  )

  private fun DomainColor.toCompose() = Color(r / 255f, g / 255f, b / 255f)
}

