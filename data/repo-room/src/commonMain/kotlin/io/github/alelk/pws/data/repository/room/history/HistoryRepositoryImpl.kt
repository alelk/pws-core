package io.github.alelk.pws.data.repository.room.history

import arrow.core.Either
import io.github.alelk.pws.database.history.HistoryDao
import io.github.alelk.pws.database.history.HistoryEntity
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.repository.HistoryObserveRepository
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HistoryRepositoryImpl(
  private val historyDao: HistoryDao,
  private val onDataChanged: () -> Unit = {},
) : HistoryReadRepository, HistoryObserveRepository, HistoryWriteRepository {

  // --- Read ---

  override suspend fun getAll(limit: Int?, offset: Int): List<HistoryEntry> =
    historyDao.getHistoryEntries(limit, offset).map { it.toDomain() }

  override suspend fun getViewCount(subject: HistorySubject): Int = 1 // Room DB doesn't track view count

  override suspend fun count(): Long = historyDao.count().toLong()

  // --- Observe ---

  override fun observeAll(limit: Int?, offset: Int): Flow<List<HistoryEntry>> =
    historyDao.getHistoryEntriesFlow().map { list ->
      val result = list.map { it.toDomain() }
      if (limit != null) result.drop(offset).take(limit) else result.drop(offset)
    }

  // --- Write ---

  override suspend fun recordView(subject: HistorySubject): Either<UpsertError, HistoryEntry> =
    when (subject) {
      is HistorySubject.BookedSong -> {
        runCatching {
          val existing = historyDao.getBySongNumberId(subject.songNumberId)
          val id = if (existing != null) {
            val updated = existing.copy(accessTimestamp = currentDateTime())
            historyDao.update(updated)
            existing.id
          } else {
            historyDao.insert(HistoryEntity(subject.songNumberId))
          }
          val entry = historyDao.getHistoryEntries(null, 0)
            .firstOrNull { it.id == id }
            ?: error("History entry not found after insert")
          Either.Right(entry.toDomain()).also { onDataChanged() }
        }.getOrElse { Either.Left(UpsertError.UnknownError(it)) }
      }
      is HistorySubject.StandaloneSong ->
        Either.Left(UpsertError.UnknownError(message = "Standalone songs not supported in local Room DB"))
    }

  override suspend fun remove(subject: HistorySubject): Either<DeleteError, HistorySubject> =
    when (subject) {
      is HistorySubject.BookedSong -> {
        runCatching {
          val entity = historyDao.getBySongNumberId(subject.songNumberId)
          if (entity != null) historyDao.delete(entity)
          Either.Right(subject)
        }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }
      }
      is HistorySubject.StandaloneSong ->
        Either.Left(DeleteError.UnknownError(message = "Standalone songs not supported"))
    }

  override suspend fun clearAll(): Either<ClearError, Int> =
    runCatching {
      val count = historyDao.count()
      historyDao.deleteAll()
      Either.Right(count)
    }.getOrElse { Either.Left(ClearError.UnknownError(it)) }
}

private fun currentDateTime(): LocalDateTime =
  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
