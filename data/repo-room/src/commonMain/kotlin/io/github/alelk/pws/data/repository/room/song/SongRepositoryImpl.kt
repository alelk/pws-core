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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
  private val songDao: SongDao,
  private val songNumberDao: SongNumberDao,
) : SongReadRepository, SongObserveRepository {

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
}


