package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.contract.favorite.FavoriteStatusDto
import io.github.alelk.pws.api.contract.favorite.FavoriteSubjectDto
import io.github.alelk.pws.api.contract.favorite.FavoriteToggleResultDto
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
 * Tests for UserFavoriteApi.
 */
@OptIn(ExperimentalTime::class)
class UserFavoriteApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  val jsonHeaders = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))

  // === list ===

  test("list() should GET /v1/user/favorites and return favorite entries") {
    val entry1 = FavoriteDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Amazing Grace",
      addedAt = Clock.System.now()
    )
    val entry2 = FavoriteDto.StandaloneSong(
      songId = SongIdDto(100),
      songName = "Standalone Song",
      addedAt = Clock.System.now()
    )
    val responseJson = json.encodeToString<List<FavoriteDto>>(listOf(entry1, entry2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/favorites"
      req.url.parameters["limit"] shouldBe "50"
      req.url.parameters["offset"] shouldBe "0"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.list()
    result.size shouldBe 2
    result[0].shouldBeInstanceOf<FavoriteDto.BookedSong>()
    result[1].shouldBeInstanceOf<FavoriteDto.StandaloneSong>()
  }

  test("list() should pass limit and offset parameters") {
    val responseJson = json.encodeToString<List<FavoriteDto>>(emptyList())

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/favorites"
      req.url.parameters["limit"] shouldBe "10"
      req.url.parameters["offset"] shouldBe "20"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    api.list(limit = 10, offset = 20)
  }

  test("list() should return empty list when no favorites") {
    val responseJson = json.encodeToString<List<FavoriteDto>>(emptyList())

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.list()
    result shouldBe emptyList()
  }

  // === clearAll ===

  test("clearAll() should DELETE /v1/user/favorites") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond("", status = HttpStatusCode.NoContent)
    }

    val api = UserFavoriteApiImpl(client)
    api.clearAll() // Should not throw
  }

  // === add ===

  test("add() should return Success with FavoriteDto for booked song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseEntry = FavoriteDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Test Song",
      addedAt = Clock.System.now()
    )
    val responseJson = json.encodeToString<FavoriteDto>(responseEntry)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond(responseJson, status = HttpStatusCode.Created, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.add(subject)
    result.shouldBeInstanceOf<ResourceUpsertResult.Success<FavoriteDto>>()
    val entry = result.resource
    entry.shouldBeInstanceOf<FavoriteDto.BookedSong>()
    entry.songNumberId shouldBe SongNumberIdDto("book1/42")
    entry.songNumber shouldBe 42
  }

  test("add() should return Success with FavoriteDto for standalone song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val responseEntry = FavoriteDto.StandaloneSong(
      songId = SongIdDto(123),
      songName = "Standalone Song",
      addedAt = Clock.System.now()
    )
    val responseJson = json.encodeToString<FavoriteDto>(responseEntry)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond(responseJson, status = HttpStatusCode.Created, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.add(subject)
    result.shouldBeInstanceOf<ResourceUpsertResult.Success<FavoriteDto>>()
    val entry = result.resource
    entry.shouldBeInstanceOf<FavoriteDto.StandaloneSong>()
    entry.songId shouldBe SongIdDto(123)
  }

  test("add() should return Success for idempotent add (already exists)") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseEntry = FavoriteDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Test Song",
      addedAt = Clock.System.now()
    )
    val responseJson = json.encodeToString<FavoriteDto>(responseEntry)

    // Server returns 201 even for existing favorites (idempotent)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      respond(responseJson, status = HttpStatusCode.Created, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.add(subject)
    result.shouldBeInstanceOf<ResourceUpsertResult.Success<FavoriteDto>>()
  }

  // === remove ===

  test("remove() should return Success for booked song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.remove(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<FavoriteSubjectDto>>()
    val deleted = result.resourceId
    deleted.shouldBeInstanceOf<FavoriteSubjectDto.BookedSong>()
    deleted.songNumberId shouldBe SongNumberIdDto("book1/42")
  }

  test("remove() should return Success for standalone song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(456))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.remove(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<FavoriteSubjectDto>>()
    val deleted = result.resourceId
    deleted.shouldBeInstanceOf<FavoriteSubjectDto.StandaloneSong>()
    deleted.songId shouldBe SongIdDto(456)
  }

  test("remove() should handle idempotent response (entry didn't exist)") {
    // Server returns 200 with the requested subject even if entry didn't exist
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/99"))
    val responseJson = json.encodeToString(subject)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites/entry"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.remove(subject)
    result.shouldBeInstanceOf<ResourceDeleteResult.Success<FavoriteSubjectDto>>()
  }

  // === getStatus ===

  test("getStatus() should return true when favorite") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseJson = json.encodeToString(FavoriteStatusDto(isFavorite = true))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/status"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.getStatus(subject)
    result shouldBe FavoriteStatusDto(isFavorite = true)
  }

  test("getStatus() should return false when not favorite") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val responseJson = json.encodeToString(FavoriteStatusDto(isFavorite = false))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/status"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.getStatus(subject)
    result shouldBe FavoriteStatusDto(isFavorite = false)
  }

  // === toggle ===

  test("toggle() should return isFavorite=true when adding") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseJson = json.encodeToString(FavoriteToggleResultDto(isFavorite = true))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.toggle(subject)
    result shouldBe FavoriteToggleResultDto(isFavorite = true)
  }

  test("toggle() should return isFavorite=false when removing") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val responseJson = json.encodeToString(FavoriteToggleResultDto(isFavorite = false))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.toggle(subject)
    result shouldBe FavoriteToggleResultDto(isFavorite = false)
  }

  test("toggle() should work for booked song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("hymnal/123"))
    val responseJson = json.encodeToString(FavoriteToggleResultDto(isFavorite = true))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.toggle(subject)
    result.isFavorite shouldBe true
  }

  test("toggle() should work for standalone song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(999))
    val responseJson = json.encodeToString(FavoriteToggleResultDto(isFavorite = false))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(responseJson, status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    val api = UserFavoriteApiImpl(client)
    val result = api.toggle(subject)
    result.isFavorite shouldBe false
  }
})
