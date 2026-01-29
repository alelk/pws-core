package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.history.HistoryEntryDto
import io.github.alelk.pws.api.contract.history.HistorySubjectDto
import io.github.alelk.pws.api.contract.history.UserHistory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * User History API client for managing user's song view history.
 * Requires user authentication.
 */
interface UserHistoryApi {

  /** Get history entries (newest first). */
  suspend fun list(limit: Int = 50, offset: Int = 0): List<HistoryEntryDto>

  /** Clear all history entries. */
  suspend fun clearAll()

  /** Record a song view. Returns the created/updated history entry. */
  suspend fun recordSongView(subject: HistorySubjectDto): ResourceUpsertResult<HistoryEntryDto>

  /** Remove a history entry. Returns the removed subject. */
  suspend fun removeSongView(subject: HistorySubjectDto): ResourceDeleteResult<HistorySubjectDto>
}

internal class UserHistoryApiImpl(client: HttpClient) : BaseResourceApi(client), UserHistoryApi {

  override suspend fun list(limit: Int, offset: Int): List<HistoryEntryDto> =
    execute<List<HistoryEntryDto>> { client.get(UserHistory(limit = limit, offset = offset)) }.getOrThrow()

  override suspend fun clearAll() {
    execute<Unit> { client.delete(UserHistory()) }.getOrThrow()
  }

  override suspend fun recordSongView(subject: HistorySubjectDto): ResourceUpsertResult<HistoryEntryDto> =
    executeUpsert<HistoryEntryDto> {
      client.post(UserHistory()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()

  override suspend fun removeSongView(subject: HistorySubjectDto): ResourceDeleteResult<HistorySubjectDto> =
    executeDelete<HistorySubjectDto>(resourceId = subject) {
      client.delete(UserHistory.Entry()) {
        contentType(ContentType.Application.Json)
        setBody(subject)
      }
    }.getOrThrow()
}

