package io.github.alelk.pws.data.repository.room.song

import io.github.alelk.pws.database.song.SongDao
import io.github.alelk.pws.database.song_number.SongNumberDao
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.database.song.SongEntity
import io.github.alelk.pws.domain.lyric.format.toText
import arrow.core.Either
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

  override suspend fun update(command: UpdateSongCommand): Either<UpdateError, SongId> =
    runCatching {
      val entity = songDao.getById(command.id) ?: return Either.Left(UpdateError.NotFound)
      val updated = entity.copy(
        version = command.version ?: entity.version,
        locale = command.locale ?: entity.locale,
        name = command.name?.value ?: entity.name,
        lyric = command.lyric?.toText(command.locale ?: entity.locale) ?: entity.lyric,
        author = when (val f = command.author) {
          is OptionalField.Unchanged -> entity.author
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        translator = when (val f = command.translator) {
          is OptionalField.Unchanged -> entity.translator
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        composer = when (val f = command.composer) {
          is OptionalField.Unchanged -> entity.composer
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        tonalities = when (val f = command.tonalities) {
          is OptionalField.Unchanged -> entity.tonalities
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        year = when (val f = command.year) {
          is OptionalField.Unchanged -> entity.year
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        bibleRef = when (val f = command.bibleRef) {
          is OptionalField.Unchanged -> entity.bibleRef
          is OptionalField.Set -> f.value
          is OptionalField.Clear -> null
        },
        edited = true
      )
      songDao.update(updated)
      Either.Right(command.id)
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(id: SongId): Either<DeleteError, SongId> =
    runCatching {
      val entity = songDao.getById(id) ?: return Either.Left(DeleteError.NotFound)
      songDao.delete(entity)
      Either.Right(id)
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
}


