package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.mapping.book.toDomain
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.query.BookQuery
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.book.repository.BookRepository
import io.github.alelk.pws.domain.core.ids.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteBookRepository(private val api: BookApi) : BookRepository {
  override fun observeBook(id: BookId): Flow<BookDetail?> = flow { emit(api.get(id.toDto())?.toDomain()) }
  override fun observeBooks(query: BookQuery, sort: BookSort): Flow<List<BookSummary>> =
    flow {
      emit(
        api.list(locale = query.locale?.toDto(), enabled = query.enabled, minPriority = query.minPriority, sort = sort.toDto())
          .map { it.toDomain() })
    }

  override suspend fun get(id: BookId): BookDetail? = api.get(id.toDto())?.toDomain()
}

