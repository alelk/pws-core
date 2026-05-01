package io.github.alelk.pws.domain.songreference.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.SongRefReason
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ReplaceAllError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.model.ReplaceAllSuccess
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.songreference.command.CreateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.command.UpdateSongReferenceCommand
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.repository.SongReferenceWriteRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ReplaceSongReferencesUseCaseTest : FunSpec({

  val tx = NoopTransactionRunner()
  val songId = SongId(100)
  val ref1 = SongReference(songId = songId, refSongId = SongId(1), reason = SongRefReason.Variation, volume = 1)
  val ref2 = SongReference(songId = songId, refSongId = SongId(2), reason = SongRefReason.Variation, volume = 2)
  val ref2Updated = ref2.copy(volume = 9)
  val ref3 = SongReference(songId = songId, refSongId = SongId(3), reason = SongRefReason.Variation, volume = 3)

  test("replace creates, updates and deletes references") {
    val read = InMemorySongReferenceReadRepository(mutableSetOf(ref1, ref2))
    val write = InMemorySongReferenceWriteRepository(read)
    val useCase = ReplaceSongReferencesUseCase(read, write, tx)

    val result = useCase(songId, listOf(ref2Updated, ref3))

    result.shouldBeInstanceOf<Either.Right<ReplaceAllSuccess<SongReference>>>()
    result.value.created shouldContainExactlyInAnyOrder listOf(ref3)
    result.value.updated shouldContainExactlyInAnyOrder listOf(ref2Updated)
    result.value.deleted shouldContainExactlyInAnyOrder listOf(ref1)
  }

  test("replace maps validation error from create") {
    val read = InMemorySongReferenceReadRepository(mutableSetOf(ref1))
    val write = InMemorySongReferenceWriteRepository(read, forceCreateValidationErrorForRefSong = ref3.refSongId)
    val useCase = ReplaceSongReferencesUseCase(read, write, tx)

    val result = useCase(songId, listOf(ref1, ref3))

    result.shouldBeInstanceOf<Either.Left<ReplaceAllError>>()
    result.value shouldBe ReplaceAllError.ValidationError("create validation")
  }
})

private class InMemorySongReferenceReadRepository(
  private val data: MutableSet<SongReference>
) : SongReferenceReadRepository {
  override suspend fun get(songId: SongId, refSongId: SongId): SongReference? =
    data.firstOrNull { it.songId == songId && it.refSongId == refSongId }

  override suspend fun getReferencesForSong(songId: SongId): List<SongReference> =
    data.filter { it.songId == songId }

  override suspend fun getReferencesToSong(refSongId: SongId): List<SongReference> =
    data.filter { it.refSongId == refSongId }

  override suspend fun exists(songId: SongId, refSongId: SongId): Boolean =
    data.any { it.songId == songId && it.refSongId == refSongId }

  override suspend fun count(): Long = data.size.toLong()

  fun put(reference: SongReference) {
    val existing = data.firstOrNull { it.songId == reference.songId && it.refSongId == reference.refSongId }
    if (existing != null) data.remove(existing)
    data.add(reference)
  }

  fun remove(songId: SongId, refSongId: SongId): SongReference? {
    val existing = data.firstOrNull { it.songId == songId && it.refSongId == refSongId }
    if (existing != null) data.remove(existing)
    return existing
  }
}

private class InMemorySongReferenceWriteRepository(
  private val read: InMemorySongReferenceReadRepository,
  private val forceCreateValidationErrorForRefSong: SongId? = null
) : SongReferenceWriteRepository {
  override suspend fun create(command: CreateSongReferenceCommand): Either<CreateError, SongReference> {
    if (forceCreateValidationErrorForRefSong == command.refSongId) {
      return Either.Left(CreateError.ValidationError("create validation"))
    }
    if (read.exists(command.songId, command.refSongId)) {
      return Either.Left(CreateError.AlreadyExists())
    }
    val created = SongReference(
      songId = command.songId,
      refSongId = command.refSongId,
      reason = command.reason,
      volume = command.volume,
      priority = command.priority
    )
    read.put(created)
    return Either.Right(created)
  }

  override suspend fun update(command: UpdateSongReferenceCommand): Either<UpdateError, SongReference> {
    val existing = read.get(command.songId, command.refSongId)
    if (existing == null) {
      return Either.Left(UpdateError.NotFound)
    }
    val updated = SongReference(
      songId = command.songId,
      refSongId = command.refSongId,
      reason = command.reason ?: existing.reason,
      volume = command.volume ?: existing.volume,
      priority = command.priority ?: existing.priority
    )
    read.put(updated)
    return Either.Right(updated)
  }

  override suspend fun delete(songId: SongId, refSongId: SongId): Either<DeleteError, SongReference> {
    val deleted = read.remove(songId, refSongId)
      ?: return Either.Left(DeleteError.NotFound)
    return Either.Right(deleted)
  }

  override suspend fun deleteAllForSong(songId: SongId): Int = 0
}




