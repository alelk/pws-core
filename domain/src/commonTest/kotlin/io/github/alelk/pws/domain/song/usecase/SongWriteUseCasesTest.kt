package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.OptionalField
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.command.UpdateSongCommand
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SongWriteUseCasesTest : FunSpec({

  val tx = NoopTransactionRunner()

  test("CreateSongUseCase returns AlreadyExists when song id exists") {
    val read = FakeSongReadRepository(existsResult = true)
    val write = FakeSongWriteRepository()
    val useCase = CreateSongUseCase(read, write, tx)

    val result = useCase(createSongCommand())

    result shouldBe Either.Left(CreateError.AlreadyExists())
    write.createCalls shouldBe 0
  }

  test("CreateSongUseCase delegates to write repository when song does not exist") {
    val read = FakeSongReadRepository(existsResult = false)
    val write = FakeSongWriteRepository(createResult = Either.Right(songId()))
    val useCase = CreateSongUseCase(read, write, tx)

    val command = createSongCommand()
    val result = useCase(command)

    result shouldBe Either.Right(command.id)
    write.lastCreateCommand shouldBe command
  }

  test("UpdateSongUseCase returns NotFound when song is absent") {
    val read = FakeSongReadRepository(getResult = null)
    val write = FakeSongWriteRepository()
    val useCase = UpdateSongUseCase(read, write, tx)

    val result = useCase(UpdateSongCommand(id = songId(), name = NonEmptyString("new")))

    result shouldBe Either.Left(UpdateError.NotFound)
    write.updateCalls shouldBe 0
  }

  test("UpdateSongUseCase validates optimistic version conflict") {
    val existing = songDetail(version = Version(1, 0))
    val read = FakeSongReadRepository(getResult = existing)
    val write = FakeSongWriteRepository()
    val useCase = UpdateSongUseCase(read, write, tx)

    val result = useCase(UpdateSongCommand(id = existing.id, name = NonEmptyString("new"), expectVersion = Version(2, 0)))

    result shouldBe Either.Left(UpdateError.ValidationError("Version conflict for song ${existing.id}: expected=2.0 actual=1.0"))
    write.updateCalls shouldBe 0
  }

  test("UpdateSongUseCase validates new version should be greater") {
    val existing = songDetail(version = Version(2, 3))
    val read = FakeSongReadRepository(getResult = existing)
    val write = FakeSongWriteRepository()
    val useCase = UpdateSongUseCase(read, write, tx)

    val result = useCase(UpdateSongCommand(id = existing.id, version = Version(2, 3), name = NonEmptyString("new")))

    result shouldBe Either.Left(UpdateError.ValidationError("Invalid version 2.3 for song ${existing.id}: expected > 2.3"))
    write.updateCalls shouldBe 0
  }

  test("UpdateSongUseCase returns Right(id) when no changes") {
    val existing = songDetail()
    val read = FakeSongReadRepository(getResult = existing)
    val write = FakeSongWriteRepository()
    val useCase = UpdateSongUseCase(read, write, tx)

    val result = useCase(UpdateSongCommand(id = existing.id, expectVersion = existing.version))

    result shouldBe Either.Right(existing.id)
    write.updateCalls shouldBe 0
  }

  test("UpdateSongUseCase applies patch and delegates updated song") {
    val existing = songDetail()
    val read = FakeSongReadRepository(getResult = existing)
    val write = FakeSongWriteRepository(updateResult = Either.Right(existing.id))
    val useCase = UpdateSongUseCase(read, write, tx)

    val command = UpdateSongCommand(
      id = existing.id,
      name = NonEmptyString("Updated"),
      author = OptionalField.Clear,
      version = Version(existing.version.major, existing.version.minor + 1)
    )

    val result = useCase(command)

    result shouldBe Either.Right(existing.id)
    write.updateCalls shouldBe 1
    write.lastUpdatedSong?.name shouldBe NonEmptyString("Updated")
    write.lastUpdatedSong?.author shouldBe null
    write.lastUpdatedSong?.edited shouldBe true
  }

  test("DeleteSongUseCase returns NotFound when song does not exist") {
    val read = FakeSongReadRepository(existsResult = false)
    val write = FakeSongWriteRepository()
    val useCase = DeleteSongUseCase(read, write, tx)

    val result = useCase(songId())

    result shouldBe Either.Left(DeleteError.NotFound)
    write.deleteCalls shouldBe 0
  }

  test("DeleteSongUseCase delegates to write repository when song exists") {
    val read = FakeSongReadRepository(existsResult = true)
    val write = FakeSongWriteRepository(deleteResult = Either.Right(songId()))
    val useCase = DeleteSongUseCase(read, write, tx)

    val id = songId()
    val result = useCase(id)

    result shouldBe Either.Right(id)
    write.lastDeletedId shouldBe id
  }
})

private fun songId() = SongId(100)

private fun baseLyric() = Lyric(Verse(setOf(1), "line"))

private fun createSongCommand() = CreateSongCommand(
  id = songId(),
  locale = Locale.EN,
  name = NonEmptyString("Song"),
  lyric = baseLyric(),
  version = Version(1, 0)
)

private fun songDetail(version: Version = Version(1, 0)) = SongDetail(
  id = songId(),
  version = version,
  locale = Locale.EN,
  name = NonEmptyString("Song"),
  lyric = baseLyric(),
  edited = false
)

private class FakeSongReadRepository(
  private val getResult: SongDetail? = songDetail(),
  private val existsResult: Boolean = false
) : SongReadRepository {

  override suspend fun get(id: SongId): SongDetail? = getResult

  override suspend fun getMany(query: SongQuery, sort: SongSort): List<SongSummary> = emptyList()

  override suspend fun getManyByIds(ids: Set<SongId>): List<SongSummary> = emptyList()

  override suspend fun exists(id: SongId): Boolean = existsResult
}

private class FakeSongWriteRepository(
  private val createResult: Either<CreateError, SongId> = Either.Right(songId()),
  private val updateResult: Either<UpdateError, SongId> = Either.Right(songId()),
  private val deleteResult: Either<DeleteError, SongId> = Either.Right(songId())
) : SongWriteRepository {

  var createCalls = 0
  var updateCalls = 0
  var deleteCalls = 0

  var lastCreateCommand: CreateSongCommand? = null
  var lastUpdatedSong: SongDetail? = null
  var lastDeletedId: SongId? = null

  override suspend fun create(command: CreateSongCommand): Either<CreateError, SongId> {
    createCalls += 1
    lastCreateCommand = command
    return createResult
  }

  override suspend fun update(song: SongDetail): Either<UpdateError, SongId> {
    updateCalls += 1
    lastUpdatedSong = song
    return updateResult
  }

  override suspend fun delete(id: SongId): Either<DeleteError, SongId> {
    deleteCalls += 1
    lastDeletedId = id
    return deleteResult
  }
}


