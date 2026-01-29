package io.github.alelk.pws.features.favorites

import androidx.compose.runtime.Immutable
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * UI representation of a favorite song.
 * Supports both booked songs (in a book) and standalone songs.
 */
@OptIn(ExperimentalTime::class)
@Immutable
sealed interface FavoriteSongUi {
  val subject: FavoriteSubject
  val songName: String
  val addedAt: Instant

  /**
   * A song favorited in context of a book.
   */
  @Immutable
  data class BookedSong(
    override val subject: FavoriteSubject.BookedSong,
    val songNumber: Int,
    override val songName: String,
    val bookDisplayName: String,
    override val addedAt: Instant
  ) : FavoriteSongUi

  /**
   * A standalone song favorited without book context.
   */
  @Immutable
  data class StandaloneSong(
    override val subject: FavoriteSubject.StandaloneSong,
    override val songName: String,
    override val addedAt: Instant
  ) : FavoriteSongUi
}

sealed interface FavoritesUiState {
  data object Loading : FavoritesUiState

  @Immutable
  data class Content(
    val songs: List<FavoriteSongUi>
  ) : FavoritesUiState

  data object Empty : FavoritesUiState
  data class Error(val message: String) : FavoritesUiState
}

sealed interface FavoritesEvent {
  data class SongClicked(val song: FavoriteSongUi) : FavoritesEvent
  data class RemoveFromFavorites(val song: FavoriteSongUi) : FavoritesEvent
}

