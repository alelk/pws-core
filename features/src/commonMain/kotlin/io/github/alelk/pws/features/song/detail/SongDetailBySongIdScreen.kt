package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import io.github.alelk.pws.domain.core.ids.SongId
import org.koin.core.parameter.parametersOf

/**
 * Song detail screen that accepts SongId instead of SongNumberId.
 *
 * Used for navigation from search results where we have SongId
 * but not the specific SongNumberId context.
 */
class SongDetailBySongIdScreen(val songId: SongId) : Screen {
  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongDetailBySongIdScreenModel>(parameters = { parametersOf(songId) })
    val state by viewModel.state.collectAsState()
    SongDetailContent(state = state)
  }
}

