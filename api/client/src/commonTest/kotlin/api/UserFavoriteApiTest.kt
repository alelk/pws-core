package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.SongNumberIdDto
import io.github.alelk.pws.api.contract.favorite.FavoriteDto
import io.github.alelk.pws.api.contract.favorite.FavoriteStatusDto
import io.github.alelk.pws.api.contract.favorite.FavoriteSubjectDto
import io.github.alelk.pws.api.contract.favorite.FavoriteToggleResultDto
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpsertError
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
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/favorites"
      req.url.parameters["limit"] shouldBe "50"
      req.url.parameters["offset"] shouldBe "0"
      respond(json.encodeToString<List<FavoriteDto>>(listOf(entry1, entry2)), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    val result = UserFavoriteApiImpl(client).list()
    result.size shouldBe 2
    result[0].shouldBeInstanceOf<FavoriteDto.BookedSong>()
    result[1].shouldBeInstanceOf<FavoriteDto.StandaloneSong>()
  }

  test("list() should pass limit and offset parameters") {
    val client = httpClientWith { req ->
      req.url.parameters["limit"] shouldBe "10"
      req.url.parameters["offset"] shouldBe "20"
      respond(json.encodeToString<List<FavoriteDto>>(emptyList()), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    UserFavoriteApiImpl(client).list(limit = 10, offset = 20)
  }

  test("list() should return empty list when no favorites") {
    val client = httpClientWith { respond(json.encodeToString<List<FavoriteDto>>(emptyList()), status = HttpStatusCode.OK, headers = jsonHeaders) }
    UserFavoriteApiImpl(client).list() shouldBe emptyList()
  }

  // === clearAll ===

  test("clearAll() should DELETE /v1/user/favorites") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond("", status = HttpStatusCode.NoContent)
    }
    UserFavoriteApiImpl(client).clearAll()
  }

  // === add ===

  test("add() should return Either.Right with FavoriteDto for booked song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseEntry = FavoriteDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Test Song",
      addedAt = Clock.System.now()
    )
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond(json.encodeToString<FavoriteDto>(responseEntry), status = HttpStatusCode.Created, headers = jsonHeaders)
    }
    val result = UserFavoriteApiImpl(client).add(subject)
    result.shouldBeInstanceOf<Either.Right<FavoriteDto>>()
    result.value.shouldBeInstanceOf<FavoriteDto.BookedSong>()
    (result.value as FavoriteDto.BookedSong).songNumberId shouldBe SongNumberIdDto("book1/42")
    (result.value as FavoriteDto.BookedSong).songNumber shouldBe 42
  }

  test("add() should return Either.Right with FavoriteDto for standalone song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val responseEntry = FavoriteDto.StandaloneSong(
      songId = SongIdDto(123),
      songName = "Standalone Song",
      addedAt = Clock.System.now()
    )
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites"
      respond(json.encodeToString<FavoriteDto>(responseEntry), status = HttpStatusCode.Created, headers = jsonHeaders)
    }
    val result = UserFavoriteApiImpl(client).add(subject)
    result.shouldBeInstanceOf<Either.Right<FavoriteDto>>()
    (result.value as FavoriteDto.StandaloneSong).songId shouldBe SongIdDto(123)
  }

  test("add() should return Either.Right for idempotent add") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val responseEntry = FavoriteDto.BookedSong(
      songNumberId = SongNumberIdDto("book1/42"),
      songNumber = 42,
      bookDisplayName = "Book One",
      songName = "Test Song",
      addedAt = Clock.System.now()
    )
    val client = httpClientWith { respond(json.encodeToString<FavoriteDto>(responseEntry), status = HttpStatusCode.Created, headers = jsonHeaders) }
    UserFavoriteApiImpl(client).add(subject).shouldBeInstanceOf<Either.Right<FavoriteDto>>()
  }

  // === remove ===

  test("remove() should return Either.Right for booked song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites/entry"
      respond(json.encodeToString(subject), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    val result = UserFavoriteApiImpl(client).remove(subject)
    result.shouldBeInstanceOf<Either.Right<FavoriteSubjectDto>>()
    (result.value as FavoriteSubjectDto.BookedSong).songNumberId shouldBe SongNumberIdDto("book1/42")
  }

  test("remove() should return Either.Right for standalone song") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(456))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/favorites/entry"
      respond(json.encodeToString(subject), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    val result = UserFavoriteApiImpl(client).remove(subject)
    result.shouldBeInstanceOf<Either.Right<FavoriteSubjectDto>>()
    (result.value as FavoriteSubjectDto.StandaloneSong).songId shouldBe SongIdDto(456)
  }

  test("remove() should handle idempotent response") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/99"))
    val client = httpClientWith { respond(json.encodeToString(subject), status = HttpStatusCode.OK, headers = jsonHeaders) }
    UserFavoriteApiImpl(client).remove(subject).shouldBeInstanceOf<Either.Right<FavoriteSubjectDto>>()
  }

  // === getStatus ===

  test("getStatus() should return true when favorite") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/status"
      respond(json.encodeToString(FavoriteStatusDto(isFavorite = true)), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    UserFavoriteApiImpl(client).getStatus(subject) shouldBe FavoriteStatusDto(isFavorite = true)
  }

  test("getStatus() should return false when not favorite") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/status"
      respond(json.encodeToString(FavoriteStatusDto(isFavorite = false)), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    UserFavoriteApiImpl(client).getStatus(subject) shouldBe FavoriteStatusDto(isFavorite = false)
  }

  // === toggle ===

  test("toggle() should return isFavorite=true when adding") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.BookedSong(SongNumberIdDto("book1/42"))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(json.encodeToString(FavoriteToggleResultDto(isFavorite = true)), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    UserFavoriteApiImpl(client).toggle(subject) shouldBe FavoriteToggleResultDto(isFavorite = true)
  }

  test("toggle() should return isFavorite=false when removing") {
    val subject: FavoriteSubjectDto = FavoriteSubjectDto.StandaloneSong(SongIdDto(123))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/favorites/toggle"
      respond(json.encodeToString(FavoriteToggleResultDto(isFavorite = false)), status = HttpStatusCode.OK, headers = jsonHeaders)
    }
    UserFavoriteApiImpl(client).toggle(subject).isFavorite shouldBe false
  }
})
