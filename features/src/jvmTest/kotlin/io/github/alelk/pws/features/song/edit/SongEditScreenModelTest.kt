package io.github.alelk.pws.features.song.edit

import arrow.core.Either
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.song.command.CreateSongCommand
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.repository.SongWriteRepository
import io.github.alelk.pws.domain.song.usecase.GetSongDetailUseCase
import io.github.alelk.pws.domain.song.usecase.UpdateSongUseCase
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.songtag.usecase.GetSongTagIdsUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Tests for [SongEditScreenModel]'s own logic — loading, required-field validation, unsaved-change
 * tracking and cancel/discard behaviour. The deep save semantics (patch building, version checks)
 * live in the use cases and are covered by domain tests, so they are out of scope here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SongEditScreenModelTest : FunSpec({

  var dispatcher = StandardTestDispatcher()
  beforeTest { dispatcher = StandardTestDispatcher(); Dispatchers.setMain(dispatcher) }
  afterTest { Dispatchers.resetMain() }

  val songId = SongId(1L)
  val song = SongDetail(
    id = songId,
    version = Version(1, 0),
    locale = Locale.of("en"),
    name = NonEmptyString("Test Song"),
    lyric = Lyric(Verse(setOf(1), "Original lyrics")),
  )

  class FakeSongRead(private val stored: SongDetail?) : SongReadRepository {
    override suspend fun get(id: SongId) = stored
    override suspend fun getMany(query: SongQuery, sort: SongSort) = emptyList<SongSummary>()
    override suspend fun getManyByIds(ids: Set<SongId>) = emptyList<SongSummary>()
    override suspend fun exists(id: SongId) = stored != null
  }

  val songWrite = object : SongWriteRepository {
    override suspend fun create(command: CreateSongCommand): Either<CreateError, SongId> = Either.Left(CreateError.UnknownError())
    override suspend fun update(song: SongDetail): Either<UpdateError, SongId> = Either.Right(song.id)
    override suspend fun delete(id: SongId): Either<DeleteError, SongId> = Either.Left(DeleteError.NotFound)
  }

  val tagObserve = object : TagObserveRepository<TagId> {
    override fun observeAll(sort: TagSort): Flow<List<Tag<TagId>>> = flowOf(emptyList())
  }

  val songTagRead = object : SongTagReadRepository<TagId> {
    override suspend fun getSongsByTag(tagId: TagId) = emptyList<io.github.alelk.pws.domain.songtag.model.SongWithBookInfo>()
    override suspend fun getTagsForSong(songId: SongId) = emptyList<Tag<TagId>>()
    override suspend fun getTagIdsBySongId(songId: SongId) = emptySet<TagId>()
    override suspend fun getSongIdsByTagId(tagId: TagId) = emptySet<SongId>()
    override suspend fun exists(songId: SongId, tagId: TagId) = false
  }

  val songTagWrite = object : SongTagWriteRepository<TagId> {
    override suspend fun create(songId: SongId, tagId: TagId): Either<CreateError, SongTagAssociation<TagId>> = Either.Left(CreateError.UnknownError())
    override suspend fun delete(songId: SongId, tagId: TagId): Either<DeleteError, SongTagAssociation<TagId>> = Either.Left(DeleteError.NotFound)
  }

  fun model(songRepo: SongReadRepository = FakeSongRead(song)): SongEditScreenModel {
    val tx = NoopTransactionRunner()
    return SongEditScreenModel(
      songId = songId,
      getSongDetailUseCase = GetSongDetailUseCase(songRepo, tx),
      updateSongUseCase = UpdateSongUseCase(songRepo, songWrite, tx),
      observeTagsUseCase = ObserveTagsUseCase(tagObserve),
      getSongTagIdsUseCase = GetSongTagIdsUseCase(songTagRead, tx),
      replaceAllSongTagsUseCase = ReplaceAllSongTagsUseCase(songTagRead, songTagWrite, tx),
    )
  }

  test("loads the song into Content with original field values") {
    runTest(dispatcher) {
      val sm = model()
      advanceUntilIdle()
      val content = sm.state.value.shouldBeInstanceOf<SongEditUiState.Content>()
      content.title shouldBe "Test Song"
      content.hasUnsavedChanges shouldBe false
    }
  }

  test("missing song produces an Error state") {
    runTest(dispatcher) {
      val sm = model(FakeSongRead(null))
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<SongEditUiState.Error>()
    }
  }

  test("saving with a blank title flags TitleRequired and does not navigate") {
    runTest(dispatcher) {
      val sm = model()
      advanceUntilIdle()
      sm.onEvent(SongEditEvent.TitleChanged(""))
      sm.onEvent(SongEditEvent.SaveClicked)
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<SongEditUiState.Content>()
        .validationMessage shouldBe SongEditValidationMessage.TitleRequired
    }
  }

  test("saving with blank text flags TextRequired") {
    runTest(dispatcher) {
      val sm = model()
      advanceUntilIdle()
      sm.onEvent(SongEditEvent.TextChanged(""))
      sm.onEvent(SongEditEvent.SaveClicked)
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<SongEditUiState.Content>()
        .validationMessage shouldBe SongEditValidationMessage.TextRequired
    }
  }

  test("editing a field then reverting it clears the unsaved-changes flag") {
    runTest(dispatcher) {
      val sm = model()
      advanceUntilIdle()

      sm.onEvent(SongEditEvent.TitleChanged("Changed"))
      sm.state.value.shouldBeInstanceOf<SongEditUiState.Content>().hasUnsavedChanges shouldBe true

      sm.onEvent(SongEditEvent.TitleChanged("Test Song"))
      sm.state.value.shouldBeInstanceOf<SongEditUiState.Content>().hasUnsavedChanges shouldBe false
    }
  }

  test("cancel with unsaved changes shows the discard dialog") {
    runTest(dispatcher) {
      val sm = model()
      advanceUntilIdle()
      sm.onEvent(SongEditEvent.TitleChanged("Changed"))
      sm.onEvent(SongEditEvent.CancelClicked)
      advanceUntilIdle()
      sm.showDiscardDialog.value shouldBe true
    }
  }
})
