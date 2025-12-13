package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.history.HistoryEntryDto
import io.github.alelk.pws.api.contract.history.RecordViewResultDto
import io.github.alelk.pws.api.contract.history.UserHistory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post

/**
 * User History API client for managing user's song view history.
 * Requires user authentication.
 */
interface UserHistoryApi {
  suspend fun list(limit: Int = 50, offset: Int = 0): List<HistoryEntryDto>
  suspend fun clearAll()
  suspend fun recordView(bookId: BookIdDto, songNumber: Int): RecordViewResultDto
  suspend fun remove(bookId: BookIdDto, songNumber: Int)
}

internal class UserHistoryApiImpl(client: HttpClient) : BaseResourceApi(client), UserHistoryApi {

  override suspend fun list(limit: Int, offset: Int): List<HistoryEntryDto> =
    execute<List<HistoryEntryDto>> { client.get(UserHistory(limit = limit, offset = offset)) }.getOrThrow()

  override suspend fun clearAll() {
    execute<Unit> { client.delete(UserHistory()) }.getOrThrow()
  }

  override suspend fun recordView(bookId: BookIdDto, songNumber: Int): RecordViewResultDto =
    execute<RecordViewResultDto> {
      client.post(UserHistory.BySongNumber(bookId = bookId, songNumber = songNumber))
    }.getOrThrow()

  override suspend fun remove(bookId: BookIdDto, songNumber: Int) {
    execute<Unit> {
      client.delete(UserHistory.BySongNumber(bookId = bookId, songNumber = songNumber))
    }.getOrThrow()
  }
}

