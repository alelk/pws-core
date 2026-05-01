package io.github.alelk.pws.domain.song.usecase

import arrow.core.Either
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.UserId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.song.command.OverrideSongCommand
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.model.MergedSongDetail
import io.github.alelk.pws.domain.song.repository.UserSongOverrideReadRepository
import io.github.alelk.pws.domain.song.repository.UserSongOverrideWriteRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class OverrideSongUseCaseTest : FunSpec({

  val tx = NoopTransactionRunner()
  val userId = UserId("user-1")
  val songId = SongId(10)

  test("returns NotFound when global song does not exist") {
    val read = OverrideFakeSongReadRepository(song = null)
    val overrideRead = FakeUserSongOverrideReadRepository(song = null)
    val overrideWrite = FakeUserSongOverrideWriteRepository()
    val getMerged = GetMergedSongDetailUseCase(read, overrideRead, tx)
    val useCase = OverrideSongUseCase(read, overrideWrite, getMerged, tx)

    val result = useCase(userId, OverrideSongCommand(songId = songId, name = NonEmptyString("new")))

    result shouldBe Either.Left(UpdateError.NotFound)
    overrideWrite.calls shouldBe 0
  }

  test("returns ValidationError when command has no changes") {
    val song = songDetail(songId)
    val read = OverrideFakeSongReadRepository(song = song)
    val overrideRead = FakeUserSongOverrideReadRepository(song = song)
    val overrideWrite = FakeUserSongOverrideWriteRepository()
    val getMerged = GetMergedSongDetailUseCase(read, overrideRead, tx)
    val useCase = OverrideSongUseCase(read, overrideWrite, getMerged, tx)

    val result = useCase(userId, OverrideSongCommand(songId = songId))

    result.shouldBeInstanceOf<Either.Left<UpdateError>>()
    result.value shouldBe UpdateError.ValidationError("No changes specified in override command")
    overrideWrite.calls shouldBe 0
  }

  test("returns merged song after successful override") {
    val globalSong = songDetail(songId, name = "Global")
    val mergedSong = songDetail(songId, name = "Personal")
    val read = OverrideFakeSongReadRepository(song = globalSong)
    val overrideRead = FakeUserSongOverrideReadRepository(song = mergedSong, hasOverrides = true)
    val overrideWrite = FakeUserSongOverrideWriteRepository(result = Either.Right(songId))
    val getMerged = GetMergedSongDetailUseCase(read, overrideRead, tx)
    val useCase = OverrideSongUseCase(read, overrideWrite, getMerged, tx)

    val result = useCase(userId, OverrideSongCommand(songId = songId, name = NonEmptyString("Personal")))

    result.shouldBeInstanceOf<Either.Right<MergedSongDetail>>()
    result.value.name shouldBe NonEmptyString("Personal")
    result.value.hasOverride shouldBe true
    overrideWrite.calls shouldBe 1
  }
})

private fun songDetail(id: SongId, name: String = "Song"): SongDetail = SongDetail(
  id = id,
  version = Version(1, 0),
  locale = Locale.EN,
  name = NonEmptyString(name),
  lyric = Lyric(Verse(setOf(1), "line")),
  edited = false
)

private class OverrideFakeSongReadRepository(private val song: SongDetail?) : SongReadRepository {
  override suspend fun get(id: SongId): SongDetail? = song?.takeIf { it.id == id }
  override suspend fun getMany(query: io.github.alelk.pws.domain.song.query.SongQuery, sort: io.github.alelk.pws.domain.song.query.SongSort) = emptyList<io.github.alelk.pws.domain.song.model.SongSummary>()
  override suspend fun getManyByIds(ids: Set<SongId>) = emptyList<io.github.alelk.pws.domain.song.model.SongSummary>()
  override suspend fun exists(id: SongId): Boolean = song?.id == id
}

private class FakeUserSongOverrideReadRepository(
  private val song: SongDetail?,
  private val hasOverrides: Boolean = false
) : UserSongOverrideReadRepository {
  override suspend fun getSongWithOverrides(userId: UserId, songId: SongId): SongDetail? = song
  override suspend fun getOverriddenSongIds(userId: UserId): List<SongId> = emptyList()
  override suspend fun hasOverrides(userId: UserId, songId: SongId): Boolean = hasOverrides
}

private class FakeUserSongOverrideWriteRepository(
  private val result: Either<UpdateError, SongId> = Either.Right(SongId(10))
) : UserSongOverrideWriteRepository {
  var calls = 0

  override suspend fun overrideSong(userId: UserId, command: OverrideSongCommand): Either<UpdateError, SongId> {
    calls += 1
    return result
  }

  override suspend fun resetOverrides(userId: UserId, songId: SongId): Either<DeleteError, SongId> =
    Either.Left(DeleteError.NotFound)

  override suspend fun clearAllOverrides(userId: UserId): Either<ClearError, Int> =
    Either.Left(ClearError.UnknownError())
}


