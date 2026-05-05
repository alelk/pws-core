package io.github.alelk.pws.features.song.detail

import arrow.core.Either
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.query.BookQuery
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.book.repository.BookObserveRepository
import io.github.alelk.pws.domain.book.usecase.ObserveBooksUseCase
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.NonEmptyString
import io.github.alelk.pws.domain.core.SongNumber
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.error.ClearError
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ToggleError
import io.github.alelk.pws.domain.core.error.UpsertError
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.ids.SongNumberId
import io.github.alelk.pws.domain.core.ids.TagId
import io.github.alelk.pws.domain.core.model.ToggleResult
import io.github.alelk.pws.domain.core.transaction.NoopTransactionRunner
import io.github.alelk.pws.domain.donationprompt.config.DonationConfig
import io.github.alelk.pws.domain.donationprompt.model.DonationPromptState
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateReadRepository
import io.github.alelk.pws.domain.donationprompt.repository.DonationPromptStateWriteRepository
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationClickedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.RecordDonationPromptDismissedUseCase
import io.github.alelk.pws.domain.donationprompt.usecase.ShouldShowDonationPromptUseCase
import io.github.alelk.pws.domain.favorite.model.Favorite
import io.github.alelk.pws.domain.favorite.model.FavoriteSong
import io.github.alelk.pws.domain.favorite.model.FavoriteSubject
import io.github.alelk.pws.domain.favorite.repository.FavoriteObserveRepository
import io.github.alelk.pws.domain.favorite.repository.FavoriteWriteRepository
import io.github.alelk.pws.domain.favorite.usecase.ObserveIsFavoriteUseCase
import io.github.alelk.pws.domain.favorite.usecase.ToggleFavoriteUseCase
import io.github.alelk.pws.domain.history.model.HistoryEntry
import io.github.alelk.pws.domain.history.model.HistorySubject
import io.github.alelk.pws.domain.history.repository.HistoryReadRepository
import io.github.alelk.pws.domain.history.repository.HistoryWriteRepository
import io.github.alelk.pws.domain.history.usecase.RecordSongViewUseCase
import io.github.alelk.pws.domain.song.lyric.Lyric
import io.github.alelk.pws.domain.song.lyric.Verse
import io.github.alelk.pws.domain.song.model.SongDetail
import io.github.alelk.pws.domain.song.model.SongSummary
import io.github.alelk.pws.domain.song.query.SongQuery
import io.github.alelk.pws.domain.song.query.SongSort
import io.github.alelk.pws.domain.song.repository.SongObserveRepository
import io.github.alelk.pws.domain.song.repository.SongReadRepository
import io.github.alelk.pws.domain.song.usecase.ObserveSongUseCase
import io.github.alelk.pws.domain.songnumber.model.SongNumberLink
import io.github.alelk.pws.domain.songnumber.repository.SongNumberReadRepository
import io.github.alelk.pws.domain.songreference.model.SongReference
import io.github.alelk.pws.domain.songreference.repository.SongReferenceReadRepository
import io.github.alelk.pws.domain.songreference.usecase.GetSongReferencesWithDetailsUseCase
import io.github.alelk.pws.domain.songtag.model.SongTagAssociation
import io.github.alelk.pws.domain.songtag.model.SongWithBookInfo
import io.github.alelk.pws.domain.songtag.repository.SongTagObserveRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagReadRepository
import io.github.alelk.pws.domain.songtag.repository.SongTagWriteRepository
import io.github.alelk.pws.domain.songtag.usecase.ObserveTagsForSongUseCase
import io.github.alelk.pws.domain.songtag.usecase.ReplaceAllSongTagsUseCase
import io.github.alelk.pws.domain.tag.model.Tag
import io.github.alelk.pws.domain.tag.query.TagSort
import io.github.alelk.pws.domain.tag.repository.TagObserveRepository
import io.github.alelk.pws.domain.tag.usecase.ObserveTagsUseCase
import io.github.alelk.pws.features.di.DonationSessionGuard
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

/**
 * Tests that [SongDetailBySongIdScreenModel] records a view in history
 * after [SongDetailBySongIdScreenModel.viewDelay] using virtual time.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SongDetailBySongIdScreenModelTest : FunSpec({

  val songId = SongId(42L)
  val songDetail = SongDetail(
    id = songId,
    version = Version(1, 0),
    locale = Locale.EN,
    name = NonEmptyString("Test Song"),
    lyric = Lyric(Verse(setOf(1), "Test lyrics")),
  )

  fun createModel(
    recordedSubjects: MutableList<HistorySubject>,
    testScope: CoroutineScope,
    viewDelaySec: Int = 5,
  ): SongDetailBySongIdScreenModel {
    val tx = NoopTransactionRunner()
    val donationConfig = DonationConfig(enabled = false, boostyUrl = "")
    val donationState = object : DonationPromptStateReadRepository, DonationPromptStateWriteRepository {
      private var state = DonationPromptState()
      override suspend fun get() = state
      override suspend fun save(state: DonationPromptState) { this.state = state }
    }
    val fakeHistoryRead = object : HistoryReadRepository {
      override suspend fun getAll(limit: Int?, offset: Int) = emptyList<HistoryEntry>()
      override suspend fun getViewCount(subject: HistorySubject) = 0
      override suspend fun count() = 0L
    }

    return SongDetailBySongIdScreenModel(
      songId = songId,
      observeSong = ObserveSongUseCase(object : SongObserveRepository {
        override fun observe(id: SongId): Flow<SongDetail?> = flowOf(songDetail)
        override fun observeAllInBook(bookId: BookId): Flow<Map<Int, SongSummary>> = flowOf(emptyMap())
      }),
      observeBooks = ObserveBooksUseCase(object : BookObserveRepository {
        override fun observe(id: BookId) = flowOf<io.github.alelk.pws.domain.book.model.BookDetail?>(null)
        override fun observeMany(query: BookQuery, sort: BookSort): Flow<List<BookSummary>> = flowOf(emptyList())
      }),
      songObserveRepository = object : SongObserveRepository {
        override fun observe(id: SongId): Flow<SongDetail?> = flowOf(songDetail)
        override fun observeAllInBook(bookId: BookId): Flow<Map<Int, SongSummary>> = flowOf(emptyMap())
      },
      recordSongView = RecordSongViewUseCase(
        historyRepository = object : HistoryWriteRepository {
          override suspend fun recordView(subject: HistorySubject): Either<UpsertError, HistoryEntry> {
            recordedSubjects.add(subject)
            return Either.Left(UpsertError.UnknownError())
          }
          override suspend fun remove(subject: HistorySubject): Either<DeleteError, HistorySubject> =
            Either.Left(DeleteError.NotFound)
          override suspend fun clearAll(): Either<ClearError, Int> =
            Either.Left(ClearError.UnknownError())
        },
        txRunner = tx,
      ),
      observeIsFavorite = ObserveIsFavoriteUseCase(object : FavoriteObserveRepository {
        override fun observeAll(limit: Int?, offset: Int): Flow<List<FavoriteSong>> = flowOf(emptyList())
        override fun observeIsFavorite(subject: FavoriteSubject): Flow<Boolean> = flowOf(false)
      }),
      toggleFavorite = ToggleFavoriteUseCase(object : FavoriteWriteRepository {
        override suspend fun add(subject: FavoriteSubject): Either<UpsertError, Favorite> = error("not called")
        override suspend fun remove(subject: FavoriteSubject): Either<DeleteError, FavoriteSubject> = error("not called")
        override suspend fun toggle(subject: FavoriteSubject): Either<ToggleError, ToggleResult<FavoriteSubject>> = error("not called")
        override suspend fun clearAll(): Either<ClearError, Int> = error("not called")
      }, tx),
      getSongReferences = GetSongReferencesWithDetailsUseCase(
        referenceRepository = object : SongReferenceReadRepository {
          override suspend fun get(songId: SongId, refSongId: SongId): SongReference? = null
          override suspend fun getReferencesForSong(songId: SongId): List<SongReference> = emptyList()
          override suspend fun getReferencesToSong(refSongId: SongId): List<SongReference> = emptyList()
          override suspend fun exists(songId: SongId, refSongId: SongId): Boolean = false
          override suspend fun count(): Long = 0L
        },
        songRepository = object : SongReadRepository {
          override suspend fun get(id: SongId): SongDetail? = null
          override suspend fun getMany(query: SongQuery, sort: SongSort): List<SongSummary> = emptyList()
          override suspend fun getManyByIds(ids: Set<SongId>): List<SongSummary> = emptyList()
          override suspend fun exists(id: SongId): Boolean = false
        },
        txRunner = tx,
      ),
      songNumberReadRepository = object : SongNumberReadRepository {
        override suspend fun getAllByBookId(bookId: BookId): List<SongNumberLink> = emptyList()
        override suspend fun getAllBySongId(songId: SongId): List<SongNumber> = emptyList()
        override suspend fun get(bookId: BookId, songId: SongId): SongNumber? = null
        override suspend fun get(link: SongNumberLink): SongNumber? = null
        override suspend fun get(link: SongNumber): SongNumberLink? = null
        override suspend fun count(bookId: BookId): Int = 0
      },
      observeTagsForSong = ObserveTagsForSongUseCase(object : SongTagObserveRepository<TagId> {
        override fun observeSongsByTag(tagId: TagId): Flow<List<SongWithBookInfo>> = flowOf(emptyList())
        override fun observeTagsForSong(songNumberId: SongNumberId): Flow<List<Tag<TagId>>> = flowOf(emptyList())
      }),
      observeAllTags = ObserveTagsUseCase(object : TagObserveRepository<TagId> {
        override fun observeAll(sort: TagSort): Flow<List<Tag<TagId>>> = flowOf(emptyList())
      }),
      replaceAllSongTags = ReplaceAllSongTagsUseCase(
        readRepository = object : SongTagReadRepository<TagId> {
          override suspend fun getSongsByTag(tagId: TagId): List<SongWithBookInfo> = emptyList()
          override suspend fun getTagsForSong(songId: SongId): List<Tag<TagId>> = emptyList()
          override suspend fun getTagIdsBySongId(songId: SongId): Set<TagId> = emptySet()
          override suspend fun getSongIdsByTagId(tagId: TagId): Set<SongId> = emptySet()
          override suspend fun exists(songId: SongId, tagId: TagId): Boolean = false
        },
        writeRepository = object : SongTagWriteRepository<TagId> {
          override suspend fun create(songId: SongId, tagId: TagId): Either<CreateError, SongTagAssociation<TagId>> = error("not called")
          override suspend fun delete(songId: SongId, tagId: TagId): Either<DeleteError, SongTagAssociation<TagId>> = error("not called")
        },
        txRunner = tx,
      ),
      shouldShowDonationPrompt = ShouldShowDonationPromptUseCase(
        config = donationConfig,
        historyReadRepository = fakeHistoryRead,
        donationStateReadRepository = donationState,
        sessionGuard = object : ShouldShowDonationPromptUseCase.SessionGuard {
          override val shownThisSession = false
        },
      ),
      recordDonationDismissed = RecordDonationPromptDismissedUseCase(
        config = donationConfig,
        historyReadRepository = fakeHistoryRead,
        donationStateReadRepository = donationState,
        donationStateWriteRepository = donationState,
      ),
      recordDonationClicked = RecordDonationClickedUseCase(
        donationStateReadRepository = donationState,
        donationStateWriteRepository = donationState,
      ),
      donationConfig = donationConfig,
      donationSessionGuard = DonationSessionGuard(),
      coroutineScope = testScope,
      viewDelay = viewDelaySec.seconds,
    )
  }

  test("does NOT record view before viewDelay elapses") {
    val recorded = mutableListOf<HistorySubject>()
    runTest {
      createModel(recorded, this)
      runCurrent()         // song loads; timer starts the 5 s countdown but does NOT fire yet
      advanceTimeBy(4_999) // 4.999 s – not yet
      recorded shouldBe emptyList()
      coroutineContext.cancelChildren() // clean up infinite StateFlow collectors
    }
  }

  test("records view for the correct song after viewDelay") {
    val recorded = mutableListOf<HistorySubject>()
    runTest {
      createModel(recorded, this)
      advanceUntilIdle()   // song loads
      advanceTimeBy(5_001) // past 5 s
      recorded.size shouldBe 1
      recorded[0] shouldBe HistorySubject.StandaloneSong(songId)
      coroutineContext.cancelChildren()
    }
  }

  test("records view exactly once even if state emits more Content updates") {
    val recorded = mutableListOf<HistorySubject>()
    runTest {
      createModel(recorded, this)
      advanceUntilIdle()
      advanceTimeBy(10_000) // well past 5 s
      recorded.size shouldBe 1
      coroutineContext.cancelChildren()
    }
  }

  test("viewDelay = 0 records view immediately after song loads") {
    val recorded = mutableListOf<HistorySubject>()
    runTest {
      createModel(recorded, this, viewDelaySec = 0)
      advanceUntilIdle() // song loads + delay(0) completes
      recorded.size shouldBe 1
      coroutineContext.cancelChildren()
    }
  }
})



