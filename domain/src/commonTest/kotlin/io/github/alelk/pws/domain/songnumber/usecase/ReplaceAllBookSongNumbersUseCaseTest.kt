package io.github.alelk.pws.domain.songnumber.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songnumber.repository.SongNumberWriteRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ReplaceAllBookSongNumbersUseCaseTest : StringSpec({

  val bookId = BookId.parse("test-book")
  val songId1 = SongId(1)
  val songId2 = SongId(2)
  val songId3 = SongId(3)
  val songId4 = SongId(4)

  val link1 = SongNumberLink(songId1, 1)
  val link2 = SongNumberLink(songId2, 2)
  val link3 = SongNumberLink(songId3, 3)
  val link4 = SongNumberLink(songId4, 4)

  // Simple in-memory implementation for testing
  class InMemorySongNumberReadRepository : SongNumberReadRepository {
    val data = mutableMapOf<BookId, MutableSet<SongNumberLink>>()

    override suspend fun getAllByBookId(bookId: BookId) = data[bookId]?.toList() ?: emptyList()
    override suspend fun getAllBySongId(songId: SongId) = throw NotImplementedError()
    override suspend fun get(bookId: BookId, songId: SongId) = throw NotImplementedError()
    override suspend fun get(link: SongNumberLink) = throw NotImplementedError()
    override suspend fun get(link: io.github.alelk.pws.domain.core.SongNumber) = throw NotImplementedError()
    override suspend fun count(bookId: BookId) = data[bookId]?.size ?: 0
  }

  class InMemorySongNumberWriteRepository(private val readRepo: InMemorySongNumberReadRepository) : SongNumberWriteRepository {
    override suspend fun create(bookId: BookId, link: SongNumberLink): Either<CreateError, SongNumberLink> {
      val set = readRepo.data.getOrPut(bookId) { mutableSetOf() }
      if (set.any { it.songId == link.songId }) return Either.Left(CreateError.AlreadyExists())
      set.add(link)
      return Either.Right(link)
    }

    override suspend fun update(bookId: BookId, link: SongNumberLink): Either<UpdateError, SongNumberLink> {
      val set = readRepo.data[bookId] ?: return Either.Left(UpdateError.NotFound)
      val existing = set.find { it.songId == link.songId } ?: return Either.Left(UpdateError.NotFound)
      set.remove(existing)
      set.add(link)
      return Either.Right(link)
    }

    override suspend fun delete(bookId: BookId, songId: SongId): Either<DeleteError, SongNumberId> {
      val id = SongNumberId(bookId, songId)
      val set = readRepo.data[bookId] ?: return Either.Left(DeleteError.NotFound)
      val existing = set.find { it.songId == songId } ?: return Either.Left(DeleteError.NotFound)
      set.remove(existing)
      return Either.Right(id)
    }
  }

  val txRunner = NoopTransactionRunner()

  "replace empty with new links - all created" {
    val readRepo = InMemorySongNumberReadRepository()
    val writeRepo = InMemorySongNumberWriteRepository(readRepo)
    val useCase = ReplaceAllBookSongNumbersUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(bookId, listOf(link1, link2, link3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongNumberLink>>>()
    result.value.created shouldContainExactlyInAnyOrder listOf(link1, link2, link3)
    result.value.updated shouldHaveSize 0
    result.value.unchanged shouldHaveSize 0
    result.value.deleted shouldHaveSize 0
  }

  "replace all existing with empty - all deleted" {
    val readRepo = InMemorySongNumberReadRepository()
    readRepo.data[bookId] = mutableSetOf(link1, link2, link3)
    val writeRepo = InMemorySongNumberWriteRepository(readRepo)
    val useCase = ReplaceAllBookSongNumbersUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(bookId, emptyList())

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongNumberLink>>>()
    result.value.created shouldHaveSize 0
    result.value.updated shouldHaveSize 0
    result.value.unchanged shouldHaveSize 0
    result.value.deleted shouldContainExactlyInAnyOrder listOf(link1, link2, link3)
  }

  "replace with same links - all unchanged" {
    val readRepo = InMemorySongNumberReadRepository()
    readRepo.data[bookId] = mutableSetOf(link1, link2, link3)
    val writeRepo = InMemorySongNumberWriteRepository(readRepo)
    val useCase = ReplaceAllBookSongNumbersUseCase(readRepo, writeRepo, txRunner)

    val result = useCase(bookId, listOf(link1, link2, link3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongNumberLink>>>()
    result.value.created shouldHaveSize 0
    result.value.updated shouldHaveSize 0
    result.value.unchanged shouldContainExactlyInAnyOrder listOf(link1, link2, link3)
    result.value.deleted shouldHaveSize 0
  }

  "replace with updated number - updated" {
    val readRepo = InMemorySongNumberReadRepository()
    readRepo.data[bookId] = mutableSetOf(link1, link2)
    val writeRepo = InMemorySongNumberWriteRepository(readRepo)
    val useCase = ReplaceAllBookSongNumbersUseCase(readRepo, writeRepo, txRunner)

    val updatedLink1 = SongNumberLink(songId1, 10) // same songId, different number

    val result = useCase(bookId, listOf(updatedLink1, link2))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongNumberLink>>>()
    result.value.created shouldHaveSize 0
    result.value.updated shouldContainExactlyInAnyOrder listOf(updatedLink1)
    result.value.unchanged shouldContainExactlyInAnyOrder listOf(link2)
    result.value.deleted shouldHaveSize 0
  }

  "mixed operations - create, update, delete, unchanged" {
    val readRepo = InMemorySongNumberReadRepository()
    readRepo.data[bookId] = mutableSetOf(link1, link2, link3) // existing: 1,2,3
    val writeRepo = InMemorySongNumberWriteRepository(readRepo)
    val useCase = ReplaceAllBookSongNumbersUseCase(readRepo, writeRepo, txRunner)

    val updatedLink2 = SongNumberLink(songId2, 20) // update song2
    // target: song2 (updated), song3 (unchanged), song4 (new)
    // deleted: song1

    val result = useCase(bookId, listOf(updatedLink2, link3, link4))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongNumberLink>>>()
    result.value.created shouldContainExactlyInAnyOrder listOf(link4)
    result.value.updated shouldContainExactlyInAnyOrder listOf(updatedLink2)
    result.value.unchanged shouldContainExactlyInAnyOrder listOf(link3)
    result.value.deleted shouldContainExactlyInAnyOrder listOf(link1)
  }
})

