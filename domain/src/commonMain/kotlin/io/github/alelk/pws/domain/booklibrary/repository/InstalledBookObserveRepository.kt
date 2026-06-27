package io.github.alelk.pws.domain.booklibrary.repository

import io.github.alelk.pws.domain.booklibrary.model.InstalledBook
import kotlinx.coroutines.flow.Flow

interface InstalledBookObserveRepository {
    fun observeAll(): Flow<List<InstalledBook>>
}
