package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import io.github.alelk.pws.api.contract.history.HistoryEntryDto
import io.github.alelk.pws.api.contract.history.HistorySubjectDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests for UserHistoryApi.
 */
@OptIn(ExperimentalTime::class)
class UserHistoryApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  val jsonHeaders = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))

  // --- list ---

  test("list() should GET /v1/user/history and return history entries") {
    val entry1 = HistoryEntryDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Amazing Grace",
      viewedAt = Clock.System.now(),
      viewCount = 3
    )
    val entry2 = HistoryEntryDto.StandaloneSong(
      songId = SongIdDto(100),
      songName = "Standalone Song",
      viewedAt = Clock.System.now(),
      viewCount = 1
    )
    val responseJson = json.encodeToString<List<HistoryEntryDto>>(listOf(entry1, entry2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/history"
      req.url.parameters["limit"] shouldBe "50"
      req.url.parameters["offset"] shouldBe "0"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.list()
    result.size shouldBe 2
    result[0].shouldBeInstanceOf<HistoryEntryDto.BookedSong>()
    result[1].shouldBeInstanceOf<HistoryEntryDto.StandaloneSong>()
  }

  test("list() should pass limit and offset parameters") {
    val responseJson = json.encodeToString<List<HistoryEntryDto>>(emptyList())

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/history"
      req.url.parameters["limit"] shouldBe "10"
      req.url.parameters["offset"] shouldBe "20"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    api.list(limit = 10, offset = 20)
  }

  // --- clearAll ---

  test("clearAll() should DELETE /v1/user/history") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/history"
      respond("", status = HttpStatusCode.NoContent)
    }

    val api = UserHistoryApiImpl(client)
    api.clearAll() // Should not throw
  }

  // --- recordSongView ---

  test("recordSongView() should return Success with HistoryEntryDto for booked song") {
    val subject: HistorySubjectDto = HistorySubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseEntry = HistoryEntryDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Test Song",
      viewedAt = Clock.System.now(),
      viewCount = 1
    )
    val responseJson = json.encodeToString<HistoryEntryDto>(responseEntry)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/history"
      respond(responseJson, status = HttpStatusCode.Created, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.recordSongView(subject)
    result.shouldBeInstanceOf<ResourceUpsertResult.Success<HistoryEntryDto>>()
    val entry = result.resource
    entry.shouldBeInstanceOf<HistoryEntryDto.BookedSong>()
    entry.songNumberId shouldBe SongNumberIdDto("book1/42")
    entry.viewCount shouldBe 1
  }

  test("recordSongView() should return Success with HistoryEntryDto for standalone song") {
    val subject: HistorySubjectDto = HistorySubjectDto.StandaloneSong(SongIdDto(123))
    val responseEntry = HistoryEntryDto.StandaloneSong(
      songId = SongIdDto(123),
      songName = "Standalone Song",
      viewedAt = Clock.System.now(),
      viewCount = 2
    )
    val responseJson = json.encodeToString<HistoryEntryDto>(responseEntry)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/history"
      respond(responseJson, status = HttpStatusCode.Created, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.recordSongView(subject)
    result.shouldBeInstanceOf<ResourceUpsertResult.Success<HistoryEntryDto>>()
    val entry = result.resource
    entry.shouldBeInstanceOf<HistoryEntryDto.StandaloneSong>()
    entry.songId shouldBe SongIdDto(123)
    entry.viewCount shouldBe 2
  }

  // --- removeSongView ---

  test("removeSongView() should return Success for booked song") {
    val subject: HistorySubjectDto = HistorySubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/history/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.removeSongView(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<HistorySubjectDto>>()
    val deleted = result.resourceId
    deleted.shouldBeInstanceOf<HistorySubjectDto.BookedSong>()
    deleted.songNumberId shouldBe SongNumberIdDto("book1/42")
  }

  test("removeSongView() should return Success for standalone song") {
    val subject: HistorySubjectDto = HistorySubjectDto.StandaloneSong(SongIdDto(456))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/history/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.removeSongView(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<HistorySubjectDto>>()
    val deleted = result.resourceId
    deleted.shouldBeInstanceOf<HistorySubjectDto.StandaloneSong>()
    deleted.songId shouldBe SongIdDto(456)
  }

  test("removeSongView() should handle idempotent response (entry didn't exist)") {
    // Server returns 200 with the requested subject even if entry didn't exist
    val subject: HistorySubjectDto = HistorySubjectDto.BookedSong(SongNumberIdDto("book1/99"))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/history/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserHistoryApiImpl(client)
    val result = api.removeSongView(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<HistorySubjectDto>>()
  }
})
