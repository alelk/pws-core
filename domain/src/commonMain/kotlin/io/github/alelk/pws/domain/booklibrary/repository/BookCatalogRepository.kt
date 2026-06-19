package io.github.alelk.pws.domain.booklibrary.repository

import io.github.alelk.pws.domain.booklibrary.model.BookCatalogEntry

interface BookCatalogRepository {
    suspend fun getAvailableBooks(): List<BookCatalogEntry>
}
