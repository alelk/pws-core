package io.github.alelk.pws.data.repository.room.favorite

import arrow.core.Either
import io.github.alelk.pws.database.favorite.FavoriteDao
import io.github.alelk.pws.database.favorite.FavoriteEntity
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.model.ToggleResult
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteReadRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FavoriteRepositoryImpl(
  private val favoriteDao: FavoriteDao,
) : FavoriteReadRepository, FavoriteObserveRepository, FavoriteWriteRepository {

  // --- Read ---

  override suspend fun getAll(limit: Int?, offset: Int): List<FavoriteSong> =
    favoriteDao.getFavoriteSongs(limit, offset).map { it.toDomain() }

  override suspend fun isFavorite(subject: FavoriteSubject): Boolean =
    when (subject) {
      is FavoriteSubject.BookedSong -> favoriteDao.isFavorite(subject.songNumberId)
      is FavoriteSubject.StandaloneSong -> false // standalone not supported in Room
    }

  override suspend fun count(): Long = favoriteDao.count().toLong()

  // --- Observe ---

  override fun observeAll(limit: Int?, offset: Int): Flow<List<FavoriteSong>> =
    favoriteDao.getFavoriteSongsFlow().map { list ->
      val result = list.map { it.toDomain() }
      if (limit != null) result.drop(offset).take(limit) else result.drop(offset)
    }

  override fun observeIsFavorite(subject: FavoriteSubject): Flow<Boolean> =
    when (subject) {
      is FavoriteSubject.BookedSong ->
        favoriteDao.getByIdFlow(subject.songNumberId).map { it != null }
      is FavoriteSubject.StandaloneSong ->
        kotlinx.coroutines.flow.flow { emit(false) }
    }

  // --- Write ---

  override suspend fun add(subject: FavoriteSubject): Either<UpsertError, Favorite> =
    when (subject) {
      is FavoriteSubject.BookedSong -> {
        runCatching {
          favoriteDao.addToFavorites(subject.songNumberId)
          val now = Clock.System.now()
          Either.Right(Favorite(subject = subject, addedAt = now))
        }.getOrElse { Either.Left(UpsertError.UnknownError(it)) }
      }
      is FavoriteSubject.StandaloneSong ->
        Either.Left(UpsertError.UnknownError(message = "Standalone songs not supported in local Room DB"))
    }

  override suspend fun remove(subject: FavoriteSubject): Either<DeleteError, FavoriteSubject> =
    when (subject) {
      is FavoriteSubject.BookedSong -> {
        runCatching {
          favoriteDao.deleteBySongNumberId(subject.songNumberId)
          Either.Right(subject)
        }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
      }
      is FavoriteSubject.StandaloneSong ->
        Either.Left(DeleteError.UnknownError(message = "Standalone songs not supported"))
    }

  override suspend fun toggle(subject: FavoriteSubject): Either<ToggleError, ToggleResult<FavoriteSubject>> =
    when (subject) {
      is FavoriteSubject.BookedSong -> {
        runCatching {
          val wasFav = favoriteDao.isFavorite(subject.songNumberId)
          favoriteDao.toggleFavorite(subject.songNumberId)
          val result: ToggleResult<FavoriteSubject> =
            if (wasFav) ToggleResult.Disabled(subject) else ToggleResult.Enabled(subject)
          Either.Right(result)
        }.getOrElse { Either.Left(ToggleError.UnknownError(it)) }
      }
      is FavoriteSubject.StandaloneSong ->
        Either.Left(ToggleError.UnknownError(message = "Standalone songs not supported"))
    }

  override suspend fun clearAll(): Either<ClearError, Int> =
    runCatching {
      val count = favoriteDao.count()
      favoriteDao.deleteAll()
      Either.Right(count)
    }.getOrElse { Either.Left(ClearError.UnknownError(it)) }
}






