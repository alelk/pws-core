package io.github.alelk.pws.features.booklibrary

import arrow.core.Either
import arrow.core.right
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.booklibrary.repository.BookCatalogRepository
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookObserveRepository
import io.github.alelk.pws.domain.booklibrary.usecase.GetBookCatalogUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.InstallBookUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.ObserveInstalledBooksUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.UninstallBookUseCase
import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.ReadError
import io.github.alelk.pws.domain.core.ids.BookId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class BookLibraryScreenModelTest : FunSpec({

  var dispatcher = StandardTestDispatcher()
  beforeTest { dispatcher = StandardTestDispatcher(); Dispatchers.setMain(dispatcher) }
  afterTest { Dispatchers.resetMain() }

  fun entry(id: String) = BookCatalogEntry(
    bookId = BookId.parse(id),
    locales = listOf(Locale.of("ru")),
    name = "Book $id",
    displayName = "Book $id",
    bundleVersion = Version(1, 0),
    downloadUrl = "https://example.test/$id.enc",
    fileSizeBytes = 100,
    checksum = "sum",
    songCount = 10,
  )

  fun installed(id: String) = InstalledBook(
    bookId = BookId.parse(id),
    source = BookInstallSource.DOWNLOADED,
    installedAt = 0,
    bundleVersion = Version(1, 0),
  )

  fun model(
    catalog: () -> Either<ReadError, List<BookCatalogEntry>>,
    installedBooks: Flow<List<InstalledBook>> = flowOf(emptyList()),
    install: (BookCatalogEntry) -> Flow<DownloadState> = { flowOf(DownloadState.Done) },
    uninstall: (BookId) -> Either<DeleteError, Unit> = { Unit.right() },
  ): BookLibraryScreenModel {
    val catalogRepo = object : BookCatalogRepository {
      override suspend fun getAvailableBooks() = catalog()
    }
    val installedRepo = object : InstalledBookObserveRepository {
      override fun observeAll() = installedBooks
    }
    val installUseCase = object : InstallBookUseCase {
      override fun invoke(entry: BookCatalogEntry) = install(entry)
    }
    val uninstallUseCase = object : UninstallBookUseCase {
      override suspend fun invoke(bookId: BookId) = uninstall(bookId)
    }
    return BookLibraryScreenModel(
      GetBookCatalogUseCase(catalogRepo),
      ObserveInstalledBooksUseCase(installedRepo),
      installUseCase,
      uninstallUseCase,
    )
  }

  test("loads catalog into Content with idle download state and no installation") {
    runTest(dispatcher) {
      val sm = model(catalog = { listOf(entry("Book-1")).right() })
      advanceUntilIdle()

      val content = sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Content>()
      content.items.single().let {
        it.bookId shouldBe BookId.parse("Book-1")
        it.installed shouldBe null
        it.downloadState shouldBe DownloadState.Idle
      }
    }
  }

  test("marks an entry as installed when present in the installed books flow") {
    runTest(dispatcher) {
      val sm = model(
        catalog = { listOf(entry("Book-1"), entry("Book-2")).right() },
        installedBooks = flowOf(listOf(installed("Book-1"))),
      )
      advanceUntilIdle()

      val items = sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Content>().items
      items.first { it.bookId == BookId.parse("Book-1") }.installed?.bookId shouldBe BookId.parse("Book-1")
      items.first { it.bookId == BookId.parse("Book-2") }.installed shouldBe null
    }
  }

  test("catalog failure produces an Error state") {
    runTest(dispatcher) {
      val sm = model(catalog = { ReadError.UnknownError(message = "offline").let { Either.Left(it) } })
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Error>().message shouldBe "offline"
    }
  }

  test("retry recovers from an error to Content") {
    runTest(dispatcher) {
      var attempt = 0
      val sm = model(catalog = {
        attempt++
        if (attempt == 1) Either.Left(ReadError.UnknownError(message = "offline"))
        else listOf(entry("Book-1")).right()
      })
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Error>()

      sm.retry()
      advanceUntilIdle()
      sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Content>()
    }
  }

  test("install updates the item's download state to Done") {
    runTest(dispatcher) {
      val sm = model(
        catalog = { listOf(entry("Book-1")).right() },
        install = { flowOf(DownloadState.Downloading(50, 100), DownloadState.Done) },
      )
      advanceUntilIdle()

      sm.install(entry("Book-1"))
      advanceUntilIdle()

      sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Content>()
        .items.single().downloadState shouldBe DownloadState.Done
    }
  }

  test("install failure surfaces as an Error download state on the item") {
    runTest(dispatcher) {
      val sm = model(
        catalog = { listOf(entry("Book-1")).right() },
        install = { flow { throw RuntimeException("network down") } },
      )
      advanceUntilIdle()

      sm.install(entry("Book-1"))
      advanceUntilIdle()

      sm.state.value.shouldBeInstanceOf<BookLibraryUiState.Content>()
        .items.single().downloadState.shouldBeInstanceOf<DownloadState.Error>()
    }
  }
})
