package io.github.alelk.pws.data.repository.room.songtag

import arrow.core.Either
import io.github.alelk.pws.database.song.SongDao
import io.github.alelk.pws.database.song_number.SongNumberDao
import io.github.alelk.pws.database.song_tag.SongTagDao
import io.github.alelk.pws.database.song_tag.SongTagEntity
import io.github.alelk.pws.database.tag.TagDao
import io.github.alelk.pws.data.repository.room.tag.toTag
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.tag.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongTagRepositoryImpl(
  private val songTagDao: SongTagDao,
  private val tagDao: TagDao,
  private val songDao: SongDao,
  private val songNumberDao: SongNumberDao,
) : SongTagReadRepository<TagId>, SongTagObserveRepository<TagId>, SongTagWriteRepository<TagId> {

  override suspend fun getSongsByTag(tagId: TagId): List<SongWithBookInfo> {
    val songNumbers = songNumberDao.getAllByTagId(tagId)
    return songNumbers.mapNotNull { sn ->
      val song = songDao.getById(sn.songId) ?: return@mapNotNull null
      SongWithBookInfo(
        songNumberId = sn.id,
        songNumber = sn.number,
        songName = song.name,
        bookDisplayName = ""  // would need book join; acceptable for now
      )
    }
  }

  override suspend fun getTagsForSong(songId: SongId): List<Tag<TagId>> {
    val tagEntities = songTagDao.getBySongId(songId)
    return tagEntities.mapNotNull { st ->
      tagDao.getById(st.tagId)?.toTag()
    }
  }

  override suspend fun getTagIdsBySongId(songId: SongId): Set<TagId> =
    songTagDao.getBySongId(songId).map { it.tagId }.toSet()

  override suspend fun getSongIdsByTagId(tagId: TagId): Set<SongId> =
    songNumberDao.getAllByTagId(tagId).map { it.songId }.toSet()

  override suspend fun exists(songId: SongId, tagId: TagId): Boolean =
    songTagDao.getById(songId, tagId) != null

  override fun observeSongsByTag(tagId: TagId): Flow<List<SongWithBookInfo>> =
    songDao.getActiveTagSongsFlow(tagId).map { list ->
      list.map { sn ->
        SongWithBookInfo(
          songNumberId = sn.songNumber.id,
          songNumber = sn.songNumber.number,
          songName = sn.song.name,
          bookDisplayName = sn.book.displayName
        )
      }
    }

  override fun observeTagsForSong(songNumberId: SongNumberId): Flow<List<Tag<TagId>>> =
    tagDao.getBySongIdFlow(songNumberId.songId).map { list -> list.map { it.toTag() } }

  override suspend fun create(songId: SongId, tagId: TagId): Either<CreateError, SongTagAssociation<TagId>> =
    runCatching {
      println("SongTagRepositoryImpl: creating association $songId - $tagId")
      songTagDao.insert(SongTagEntity(songId = songId, tagId = tagId))
      Either.Right(SongTagAssociation(songId, tagId))
    }.getOrElse {
      println("SongTagRepositoryImpl: error creating association: ${it.message}")
      Either.Left(CreateError.UnknownError(it))
    }

  override suspend fun delete(songId: SongId, tagId: TagId): Either<DeleteError, SongTagAssociation<TagId>> =
    runCatching {
      println("SongTagRepositoryImpl: deleting association $songId - $tagId")
      val entity = songTagDao.getById(songId, tagId)
        ?: return Either.Left(DeleteError.NotFound)
      songTagDao.delete(entity)
      Either.Right(SongTagAssociation(songId, tagId))
    }.getOrElse {
      println("SongTagRepositoryImpl: error deleting association: ${it.message}")
      Either.Left(DeleteError.UnknownError(it))
    }
}


