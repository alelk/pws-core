package io.github.alelk.pws.data.repository.room.tag

import arrow.core.Either
import io.github.alelk.pws.database.tag.TagDao
import io.github.alelk.pws.database.tag.TagEntity
import io.github.alelk.pws.database.song_tag.SongTagDao
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.tag.command.CreateTagCommand
import io.github.alelk.pws.domain.tag.command.UpdateTagCommand
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.model.TagDetail
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.repository.TagReadRepository
import io.github.alelk.pws.domain.tag.repository.TagWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepositoryImpl(
  private val tagDao: TagDao,
  private val songTagDao: SongTagDao,
) : TagReadRepository<TagId>, TagObserveRepository<TagId>, TagWriteRepository<TagId> {

  override suspend fun get(id: TagId): TagDetail<TagId>? {
    val entity = tagDao.getById(id) ?: return null
    val songCount = songTagDao.getAll(Int.MAX_VALUE, 0).count { it.tagId == id }
    return entity.toTagDetail(songCount)
  }

  override suspend fun getAll(sort: TagSort): List<Tag<TagId>> {
    val entities = tagDao.getAll(Int.MAX_VALUE, 0)
    return entities.map { it.toTag() }.sortedWith(sort.comparator())
  }

  override fun observeAll(sort: TagSort): Flow<List<Tag<TagId>>> =
    tagDao.getAllFlow().map { list ->
      list.map { it.toTag() }.sortedWith(sort.comparator())
    }

  override suspend fun create(command: CreateTagCommand<TagId>): Either<CreateError, TagId> =
    runCatching {
      val entity = TagEntity(
        id = command.id,
        name = command.name,
        priority = command.priority,
        color = command.color,
        predefined = command.id is TagId.Predefined
      )
      tagDao.insert(entity)
      Either.Right(command.id)
    }.getOrElse { Either.Left(CreateError.UnknownError(it)) }

  override suspend fun update(command: UpdateTagCommand<TagId>): Either<UpdateError, TagId> =
    runCatching {
      val existing = tagDao.getById(command.id)
        ?: return Either.Left(UpdateError.NotFound)
      val updated = existing.copy(
        name = command.name ?: existing.name,
        color = command.color ?: existing.color,
        priority = command.priority ?: existing.priority,
      )
      tagDao.update(updated)
      Either.Right(command.id)
    }.getOrElse { Either.Left(UpdateError.UnknownError(it)) }

  override suspend fun delete(id: TagId): Either<DeleteError, TagId> =
    runCatching {
      val existing = tagDao.getById(id)
        ?: return Either.Left(DeleteError.NotFound)
      tagDao.delete(existing)
      Either.Right(id)
    }.getOrElse { Either.Left(DeleteError.UnknownError(it)) }

  private fun TagSort.comparator(): Comparator<Tag<TagId>> = when (this) {
    TagSort.ByName -> compareBy { it.name }
    TagSort.ByPriority -> compareByDescending { it.priority }
    TagSort.BySongCount -> compareBy { it.name } // fallback
  }
}



