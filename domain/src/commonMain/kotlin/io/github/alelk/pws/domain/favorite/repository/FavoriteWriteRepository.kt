package io.github.alelk.pws.domain.favorite.repository

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.model.ToggleResult
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject

/**
 * Mutation operations for Favorites.
 */
interface FavoriteWriteRepository {
  /**
   * Add song to favorites.
   */
  suspend fun add(subject: FavoriteSubject): Either<UpsertError, Favorite>

  /**
   * Remove song from favorites.
   */
  suspend fun remove(subject: FavoriteSubject): Either<DeleteError, FavoriteSubject>

  /**
   * Toggle favorite status.
   */
  suspend fun toggle(subject: FavoriteSubject): Either<ToggleError, ToggleResult<FavoriteSubject>>

  /**
   * Clear all favorites.
   */
  suspend fun clearAll(): Either<ClearError, Int>
}
