package io.github.alelk.pws.domain.booklibrary.usecase

import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import io.github.alelk.pws.domain.booklibrary.repository.InstalledBookObserveRepository
import kotlinx.coroutines.flow.Flow

class ObserveInstalledBooksUseCase(
    private val repository: InstalledBookObserveRepository,
) {
    operator fun invoke(): Flow<List<InstalledBook>> = repository.observeAll()
}
