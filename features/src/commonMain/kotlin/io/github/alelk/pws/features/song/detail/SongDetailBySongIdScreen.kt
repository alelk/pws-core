package io.github.alelk.pws.features.song.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
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
class SongDetailBySongIdScreen(val songIdLong: Long) : Screen {
  val songId: SongId get() = SongId(songIdLong)

  override val key: String = "song-detail-by-id/$songIdLong"

  @Composable
  override fun Content() {
    val viewModel = koinScreenModel<SongDetailBySongIdScreenModel>(parameters = { parametersOf(songId) })
    val state by viewModel.state.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val uriHandler = LocalUriHandler.current

    val content = state as? SongDetailUiState.Content
    val donationBoostyUrl = content?.donationBoostyUrl.orEmpty()

    SongDetailContent(
      state = state,
      isFavorite = content?.isFavorite == true,
      references = content?.references.orEmpty(),
      referenceBookContexts = content?.referenceBookContexts.orEmpty(),
      songTags = content?.songTags.orEmpty(),
      allTags = allTags,
      onFavoriteClick = { viewModel.onToggleFavorite() },
      onSaveTags = { viewModel.onSaveTags(it) },
      onDonationDonate = {
        viewModel.onDonationClicked()
        if (donationBoostyUrl.isNotBlank()) uriHandler.openUri(donationBoostyUrl)
      },
      onDonationDismiss = { viewModel.onDonationDismissed() },
    )
  }
}
