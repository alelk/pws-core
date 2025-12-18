package io.github.alelk.pws.domain.songtag.usecase

import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.core.result.DeleteResourceResult
import io.github.alelk.pws.domain.core.result.ReplaceAllResourcesResult
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ReplaceAllSongTagsUseCaseTest : StringSpec({

  val songId = SongId(1)
  val tagId1 = TagId.parse("tag-1")
  val tagId2 = TagId.parse("tag-2")
  val tagId3 = TagId.parse("tag-3")
  val tagId4 = TagId.parse("tag-4")

  // Simple in-memory implementation for testing
  class InMemorySongTagReadRepository : SongTagReadRepository {
    val data = mutableMapOf<SongId, MutableSet<TagId>>()

    override suspend fun getTagIdsBySongId(songId: SongId) = data[songId]?.toSet() ?: emptySet()
    override suspend fun getSongIdsByTagId(tagId: TagId) =
      data.filterValues { tagId in it }.keys.toSet()
    override suspend fun exists(songId: SongId, tagId: TagId) =
      data[songId]?.contains(tagId) == true
  }

  class InMemorySongTagWriteRepository(private val readRepo: InMemorySongTagReadRepository) : SongTagWriteRepository {
    override suspend fun create(songId: SongId, tagId: TagId): CreateResourceResult<SongTagAssociation> {
      val association = SongTagAssociation(songId, tagId)
      val set = readRepo.data.getOrPut(songId) { mutableSetOf() }
      if (tagId in set) return CreateResourceResult.AlreadyExists(association)
      set.add(tagId)
      return CreateResourceResult.Success(association)
    }

    override suspend fun delete(songId: SongId, tagId: TagId): DeleteResourceResult<SongTagAssociation> {
      val association = SongTagAssociation(songId, tagId)
      val set = readRepo.data[songId] ?: return DeleteResourceResult.NotFound(association)
      if (tagId !in set) return DeleteResourceResult.NotFound(association)
      set.remove(tagId)
      return DeleteResourceResult.Success(association)
    }
  }

  val txRunner = NoopTransactionRunner()

  "replace empty with new tags - all created" {
    val readRepo = InMemorySongTagReadRepository()
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId1, tagId2, tagId3))

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
    result.updated shouldHaveSize 0
    result.unchanged shouldHaveSize 0
    result.deleted shouldHaveSize 0
  }

  "replace all existing with empty - all deleted" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, emptySet())

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created shouldHaveSize 0
    result.updated shouldHaveSize 0
    result.unchanged shouldHaveSize 0
    result.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
  }

  "replace with same tags - all unchanged" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId1, tagId2, tagId3))

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created shouldHaveSize 0
    result.updated shouldHaveSize 0
    result.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1, tagId2, tagId3)
    result.deleted shouldHaveSize 0
  }

  "mixed operations - create, delete, unchanged" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2, tagId3) // existing: 1,2,3
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    // target: tag2 (unchanged), tag3 (unchanged), tag4 (new)
    // deleted: tag1

    val result = useCase(songId, setOf(tagId2, tagId3, tagId4))

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId4)
    result.updated shouldHaveSize 0 // song-tag has no updatable fields
    result.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId2, tagId3)
    result.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1)
  }

  "partial overlap - some created, some unchanged, some deleted" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1, tagId2) // existing: 1,2
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    // target: tag2 (unchanged), tag3 (new), tag4 (new)
    // deleted: tag1

    val result = useCase(songId, setOf(tagId2, tagId3, tagId4))

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId3, tagId4)
    result.unchanged.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId2)
    result.deleted.map { it.tagId } shouldContainExactlyInAnyOrder listOf(tagId1)
  }

  "all associations are for correct song" {
    val readRepo = InMemorySongTagReadRepository()
    readRepo.data[songId] = mutableSetOf(tagId1)
    val writeRepo = InMemorySongTagWriteRepository(readRepo)
    val useCase = ReplaceAllSongTagsUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(songId, setOf(tagId2, tagId3))

    result.shouldBeInstanceOf<ReplaceAllResourcesResult.Success<SongTagAssociation>>()
    result.created.forEach { it.songId shouldBe songId }
    result.deleted.forEach { it.songId shouldBe songId }
  }
})

