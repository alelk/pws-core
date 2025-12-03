package io.github.alelk.pws.api.client.repository

import io.github.alelk.pws.api.client.api.BookApi
import io.github.alelk.pws.api.mapping.book.toDomain
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.BookDetail
import io.github.alelk.pws.domain.book.model.BookSummary
import io.github.alelk.pws.domain.book.query.BookQuery
import io.github.alelk.pws.domain.book.query.BookSort
import io.github.alelk.pws.domain.book.repository.BookReadRepository
import io.github.alelk.pws.domain.core.ids.BookId

class RemoteBookReadRepository(private val api: BookApi) : BookReadRepository {

  override suspend fun get(id: BookId): BookDetail? =
    api.get(id.toDto())?.toDomain()

  override suspend fun getMany(query: BookQuery, sort: BookSort): List<BookSummary> =
    api.list(locale = query.locale?.toDto(), minPriority = query.minPriority, sort = sort.toDto()).map { it.toDomain() }
}