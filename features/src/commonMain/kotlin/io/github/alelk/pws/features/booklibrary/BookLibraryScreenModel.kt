package io.github.alelk.pws.features.booklibrary

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.booklibrary.model.DownloadState
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookRepository
import io.github.alelk.pws.domain.booklibrary.usecase.GetBookCatalogUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.InstallBookUseCase
import io.github.alelk.pws.domain.booklibrary.usecase.UninstallBookUseCase
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookLibraryScreenModel(
    private val getBookCatalog: GetBookCatalogUseCase,
    private val installedBookRepository: InstalledBookRepository,
    private val installBook: InstallBookUseCase,
    private val uninstallBook: UninstallBookUseCase,
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

    private fun load() {
        screenModelScope.launch {
            runCatching { getBookCatalog.getCatalog() }
                .onFailure { mutableState.value = BookLibraryUiState.Error(it.message ?: "Unknown error") }
                .onSuccess { entries ->
                    catalog = entries
                    installedBookRepository.observeAll().collectLatest { installedList ->
                        val installedMap = installedList.associateBy { it.bookId }
                        val current = mutableState.value
                        val downloadStates = if (current is BookLibraryUiState.Content)
                            current.items.associate { it.bookId to it.downloadState }
                        else emptyMap()

                        mutableState.value = BookLibraryUiState.Content(
                            entries.map { entry ->
                                BookLibraryItem(
                                    entry = entry,
                                    installed = installedMap[entry.bookId],
                                    downloadState = downloadStates[entry.bookId] ?: DownloadState.Idle,
                                )
                            }
                        )
                    }
                }
        }
    }

    fun install(entry: BookCatalogEntry) {
        val bookId = entry.bookId
        if (downloadJobs[bookId]?.isActive == true) return
        downloadJobs[bookId] = screenModelScope.launch {
            installBook(entry).collect { state ->
                updateDownloadState(bookId, state)
            }
        }
    }

    fun uninstall(bookId: BookId) {
        screenModelScope.launch {
            runCatching { uninstallBook(bookId) }
                .onFailure { updateDownloadState(bookId, DownloadState.Error(it.message ?: "Uninstall failed")) }
        }
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
