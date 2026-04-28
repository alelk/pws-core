package io.github.alelk.pws.data.repository.room.songnumber

import arrow.core.Either
import io.github.alelk.pws.database.song_number.SongNumberDao
import io.github.alelk.pws.database.song_number.SongNumberEntity
import io.github.alelk.pws.domain.core.SongNumber
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository

class SongNumberRepositoryImpl(
  private val songNumberDao: SongNumberDao,
) : SongNumberReadRepository, SongNumberWriteRepository {

  override suspend fun getAllByBookId(bookId: BookId): List<SongNumberLink> =
    songNumberDao.getByBookIds(listOf(bookId)).map { SongNumberLink(it.songId, it.number) }

  override suspend fun getAllBySongId(songId: SongId): List<SongNumber> =
    songNumberDao.getBySongId(songId).map { SongNumber(it.bookId, it.number) }

  override suspend fun get(bookId: BookId, songId: SongId): SongNumber? =
    songNumberDao.getById(bookId, songId)?.let { SongNumber(it.bookId, it.number) }

  override suspend fun get(link: SongNumberLink): SongNumber? =
    songNumberDao.getBySongId(link.songId)
      .firstOrNull()
      ?.let { SongNumber(it.bookId, it.number) }

  override suspend fun get(link: SongNumber): SongNumberLink? =
    songNumberDao.getByBookIdAndSongNumber(link.bookId, link.number)
      ?.let { SongNumberLink(it.songId, it.number) }

  override suspend fun count(bookId: BookId): Int =
    songNumberDao.getByBookIds(listOf(bookId)).size

  override suspend fun create(bookId: BookId, link: SongNumberLink): Either<CreateError, SongNumberLink> =
    runCatching {
      songNumberDao.insert(SongNumberEntity(bookId = bookId, songId = link.songId, number = link.number, priority = 0))
      Either.Right(link)
    }.getOrElse { Either.Left(CreateError.UnknownError(it)) }

  override suspend fun update(bookId: BookId, link: SongNumberLink): Either<UpdateError, SongNumberLink> =
    runCatching {
      val existing = songNumberDao.getById(bookId, link.songId)
        ?: return Either.Left(UpdateError.NotFound)
      songNumberDao.update(existing.copy(number = link.number))
      Either.Right(link)
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(bookId: BookId, songId: SongId): Either<DeleteError, SongNumberId> =
    runCatching {
      val existing = songNumberDao.getById(bookId, songId)
        ?: return Either.Left(DeleteError.NotFound)
      songNumberDao.delete(existing)
      Either.Right(existing.id)
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
}


