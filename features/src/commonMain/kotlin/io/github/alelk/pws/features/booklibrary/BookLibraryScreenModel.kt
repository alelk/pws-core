package io.github.alelk.pws.features.booklibrary

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.booklibrary.usecase.GetBookCatalogUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.InstallBookUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.ObserveInstalledBooksUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.UninstallBookUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.UpdateBookUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.telemetry.NoOpTelemetry
import io.github.alelk.pws.domain.telemetry.Telemetry
import io.github.alelk.pws.domain.telemetry.TelemetryAttr
import io.github.alelk.pws.domain.telemetry.TelemetryEvent
import io.github.alelk.pws.domain.telemetry.TelemetryResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookLibraryScreenModel(
    private val getBookCatalog: GetBookCatalogUseCase,
    private val observeInstalledBooks: ObserveInstalledBooksUseCase,
    private val installBook: InstallBookUseCase,
    private val uninstallBook: UninstallBookUseCase,
    private val updateBook: UpdateBookUseCase,
    private val deviceLanguage: String = "",
    private val telemetry: Telemetry = NoOpTelemetry,
) : StateScreenModel<BookLibraryUiState>(BookLibraryUiState.Loading) {

    private var catalog: List<BookCatalogEntry> = emptyList()
    private val downloadJobs = mutableMapOf<BookId, Job>()

    init {
        load()
    }

    fun retry() {
        mutableState.value = BookLibraryUiState.Loading
        load()
    }

    private val deviceLang = deviceLanguage.substringBefore('-').lowercase()

    private fun matchesLocale(entry: BookCatalogEntry): Boolean =
        deviceLang.isNotEmpty() && entry.locales.any { it.value.substringBefore('-').lowercase() == deviceLang }

    private fun load() {
        screenModelScope.launch {
            getBookCatalog().fold(
                ifLeft = { error ->
                    mutableState.value = BookLibraryUiState.Error(error.message)
                    // Catalog unreachable/unparseable is the single failure that blocks the whole
                    // library screen — report it as non-fatal so it shows up before users complain.
                    telemetry.recordError(
                        IllegalStateException(error.message),
                        "book_catalog_load_failed",
                        mapOf(TelemetryAttr.STAGE to "catalog"),
                    )
                },
                ifRight = { entries ->
                    catalog = entries
                    // Books matching the device language float to the top (stable within each group).
                    val sortedEntries = entries.sortedWith(compareByDescending { matchesLocale(it) })

                    observeInstalledBooks().collectLatest { installedList ->
                        val installedMap = installedList.associateBy { it.bookId }
                        val downloadStates = (mutableState.value as? BookLibraryUiState.Content)
                            ?.items?.associate { it.bookId to it.downloadState }
                            ?: emptyMap()

                        // Recommend the top locale-matched book only when nothing is installed yet.
                        val recommended =
                            if (installedList.isEmpty()) sortedEntries.firstOrNull { matchesLocale(it) }?.bookId
                            else null

                        mutableState.value = BookLibraryUiState.Content(
                            items = sortedEntries.map { entry ->
                                BookLibraryItem(
                                    entry = entry,
                                    installed = installedMap[entry.bookId],
                                    downloadState = downloadStates[entry.bookId] ?: DownloadState.Idle,
                                )
                            },
                            recommendedBookId = recommended,
                        )
                    }
                },
            )
        }
    }

    fun install(entry: BookCatalogEntry) {
        val bookId = entry.bookId
        if (downloadJobs[bookId]?.isActive == true) return
        telemetry.log("book_install_started")
        downloadJobs[bookId] = screenModelScope.launch {
            installBook(entry)
                .catch { e ->
                    updateDownloadState(bookId, DownloadState.Error(e.message ?: "Unknown error"))
                    reportBookOutcome(TelemetryEvent.BOOK_INSTALL, bookId, e)
                }
                .collect { state ->
                    updateDownloadState(bookId, state)
                    reportTerminalState(TelemetryEvent.BOOK_INSTALL, bookId, state)
                }
        }
    }

    fun update(entry: BookCatalogEntry) {
        val bookId = entry.bookId
        if (downloadJobs[bookId]?.isActive == true) return
        telemetry.log("book_update_started")
        downloadJobs[bookId] = screenModelScope.launch {
            updateBook(entry)
                .catch { e ->
                    updateDownloadState(bookId, DownloadState.Error(e.message ?: "Unknown error"))
                    reportBookOutcome(TelemetryEvent.BOOK_UPDATE, bookId, e)
                }
                .collect { state ->
                    updateDownloadState(bookId, state)
                    reportTerminalState(TelemetryEvent.BOOK_UPDATE, bookId, state)
                }
        }
    }

    fun uninstall(bookId: BookId) {
        screenModelScope.launch {
            uninstallBook(bookId).fold(
                ifLeft = { error ->
                    updateDownloadState(bookId, DownloadState.Error(error.message))
                    reportBookOutcome(TelemetryEvent.BOOK_UNINSTALL, bookId, IllegalStateException(error.message))
                },
                ifRight = {
                    telemetry.event(
                        TelemetryEvent.BOOK_UNINSTALL,
                        mapOf(TelemetryAttr.BOOK_ID to bookId.toString(), TelemetryAttr.RESULT to TelemetryResult.OK),
                    )
                },
            )
        }
    }

    /** Turns a terminal [DownloadState] into the matching analytics event (intermediate states are ignored). */
    private fun reportTerminalState(event: String, bookId: BookId, state: DownloadState) {
        when (state) {
            is DownloadState.Done ->
                telemetry.event(
                    event,
                    mapOf(TelemetryAttr.BOOK_ID to bookId.toString(), TelemetryAttr.RESULT to TelemetryResult.OK),
                )

            is DownloadState.Error -> reportBookOutcome(event, bookId, IllegalStateException(state.message))
            else -> Unit
        }
    }

    /** Records both the failed-outcome event and a non-fatal, so failures are visible in stats and in errors. */
    private fun reportBookOutcome(event: String, bookId: BookId, error: Throwable) {
        telemetry.event(
            event,
            mapOf(TelemetryAttr.BOOK_ID to bookId.toString(), TelemetryAttr.RESULT to TelemetryResult.ERROR),
        )
        telemetry.recordError(error, "${event}_failed", mapOf(TelemetryAttr.BOOK_ID to bookId.toString()))
    }

    private fun updateDownloadState(bookId: BookId, state: DownloadState) {
        val current = mutableState.value as? BookLibraryUiState.Content ?: return
        mutableState.value = current.copy(
            items = current.items.map { item ->
                if (item.bookId == bookId) item.copy(downloadState = state) else item
            }
        )
    }
}
