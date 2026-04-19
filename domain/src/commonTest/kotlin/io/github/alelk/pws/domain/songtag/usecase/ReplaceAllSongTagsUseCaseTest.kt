package io.github.alelk.pws.domain.songtag.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.tag.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ReplaceAllSongTagsUseCaseTest : StringSpec({

  val songId = SongId(1)
  val tagId1 = TagId.Predefined("tag-1")
  val tagId2 = TagId.Predefined("tag-2")
  val tagId3 = TagId.Predefined("tag-3")
  val tagId4 = TagId.Predefined("tag-4")

  // Simple in-memory implementation for testing
  class InMemorySongTagReadRepository : SongTagReadRepository<TagId.Predefined> {
    val data = mutableMapOf<SongId, MutableSet<TagId.Predefined>>()

    override suspend fun getSongsByTag(tagId: TagId.Predefined) = emptyList<io.github.alelk.pws.domain.songtag.model.SongWithBookInfo>()
    override suspend fun getTagsForSong(songId: SongId) = emptyList<Tag.Predefined>()
    override suspend fun getTagIdsBySongId(songId: SongId) = data[songId]?.toSet() ?: emptySet()
    override suspend fun getSongIdsByTagId(tagId: TagId.Predefined) =
      data.filterValues { tagId in it }.keys.toSet()
    override suspend fun exists(songId: SongId, tagId: TagId.Predefined) =
      data[songId]?.contains(tagId) == true
  }

  class InMemorySongTagWriteRepository(private val readRepo: InMemorySongTagReadRepository) : SongTagWriteRepository<TagId.Predefined> {
    override suspend fun create(songId: SongId, tagId: TagId.Predefined): Either<CreateError, SongTagAssociation<TagId.Predefined>> {
      val association = SongTagAssociation(songId, tagId)
      val set = readRepo.data.getOrPut(songId) { mutableSetOf() }
      if (tagId in set) return Either.Left(CreateError.AlreadyExists())
      set.add(tagId)
      return Either.Right(association)
    }

    override suspend fun delete(songId: SongId, tagId: TagId.Predefined): Either<DeleteError, SongTagAssociation<TagId.Predefined>> {
      val association = SongTagAssociation(songId, tagId)
      val set = readRepo.data[songId] ?: return Either.Left(DeleteError.NotFound)
      if (tagId !in set) return Either.Left(DeleteError.NotFound)
      set.remove(tagId)
      return Either.Right(association)
    }
  }

  val txRunner = NoopTransactionRunner()

  "replace empty with new tags - all created" {
    val readRepo = InMemorySongTagReadRepository()
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId1, tagId2, tagId3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
    result.value.updated shouldHaveSize 0
    result.value.unchanged shouldHaveSize 0
    result.value.deleted shouldHaveSize 0
  }

  "replace all existing with empty - all deleted" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, emptySet())

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created shouldHaveSize 0
    result.value.updated shouldHaveSize 0
    result.value.unchanged shouldHaveSize 0
    result.value.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
  }

  "replace with same tags - all unchanged" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId1, tagId2, tagId3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created shouldHaveSize 0
    result.value.updated shouldHaveSize 0
    result.value.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
    result.value.deleted shouldHaveSize 0
  }

  "mixed operations - create, delete, unchanged" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId2, tagId3, tagId4))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId4)
    result.value.updated shouldHaveSize 0 // song-tag has no updatable fields
    result.value.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId2, tagId3)
    result.value.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1)
  }

  "partial overlap - some created, some unchanged, some deleted" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId2, tagId3, tagId4))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId3, tagId4)
    result.value.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId2)
    result.value.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1)
  }

  "all associations are for correct song" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId2, tagId3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongTagAssociation<TagId.Predefined>>>>()
    result.value.created.forEach { it.songId shouldBe songId }
    result.value.deleted.forEach { it.songId shouldBe songId }
  }
})
