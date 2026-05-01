package io.github.alelk.pws.data.repository.room.song

import arrow.core.Either
import io.github.alelk.pws.database.song.SongDao
import io.github.alelk.pws.database.song.SongEntity
import io.github.alelk.pws.database.song_number.SongNumberDao
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.lyric.format.toText
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
  private val songDao: SongDao,
  private val songNumberDao: SongNumberDao,
) : SongReadRepository, SongObserveRepository, SongWriteRepository {

  override suspend fun get(id: SongId): SongDetail? =
    songDao.getById(id)?.toDomain()

  override suspend fun getMany(query: SongQuery, sort: SongSort): List<SongSummary> {
    val entities = if (query.bookId != null) {
      val numbers = songNumberDao.getByBookIds(listOfNotNull(query.bookId))
        .map { it.songId }
      songDao.getByIds(numbers)
    } else {
      // fetch all pages
      val result = mutableListOf<io.github.alelk.pws.database.song.SongEntity>()
      var offset = 0
      val pageSize = 500
      while (true) {
        val page = songDao.getAll(pageSize, offset)
        result.addAll(page)
        if (page.size < pageSize) break
        offset += pageSize
      }
      result
    }
    return entities.map { it.toSummary() }
  }

  override suspend fun getManyByIds(ids: Set<SongId>): List<SongSummary> {
    if (ids.isEmpty()) return emptyList()
    return songDao.getByIds(ids.toList()).map { it.toSummary() }
  }

  override suspend fun exists(id: SongId): Boolean =
    songDao.getById(id) != null

  override fun observe(id: SongId): Flow<SongDetail?> =
    songDao.getByIdFlow(id).map { it?.toDomain() }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun observeAllInBook(bookId: BookId): Flow<Map<Int, SongSummary>> =
    songNumberDao.getBookSongsByBookIdFlow(bookId).map { list ->
      list.associate { it.songNumber.number to it.song.toSummary() }
    }

  override suspend fun create(command: CreateSongCommand): Either<CreateError, SongId> =
    runCatching {
      val entity = SongEntity(
        id = command.id,
        version = command.version,
        locale = command.locale,
        name = command.name.value,
        lyric = command.lyric.toText(command.locale),
        author = command.author,
        translator = command.translator,
        composer = command.composer,
        tonalities = command.tonalities,
        year = command.year,
        bibleRef = command.bibleRef,
        edited = command.edited
      )
      songDao.insert(entity)
      Either.Right(command.id)
    }.getOrElse { Either.Left(CreateError.UnknownError(it)) }

  override suspend fun update(song: SongDetail): Either<UpdateError, SongId> =
    runCatching {
      val entity = SongEntity(
        id = song.id,
        version = song.version,
        locale = song.locale,
        name = song.name.value,
        lyric = song.lyric.toText(song.locale),
        author = song.author,
        translator = song.translator,
        composer = song.composer,
        tonalities = song.tonalities,
        year = song.year,
        bibleRef = song.bibleRef,
        edited = song.edited
      )
      songDao.update(entity)
      Either.Right(song.id)
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(id: SongId): Either<DeleteError, SongId> =
    runCatching {
      songDao.deleteById(id)
      Either.Right(id)
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
}


