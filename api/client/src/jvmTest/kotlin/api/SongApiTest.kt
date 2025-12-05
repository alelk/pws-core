package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.LyricDto
import io.github.alelk.pws.api.contract.song.LyricPartDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongDetailDto
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.songDetail
import io.github.alelk.pws.domain.song.model.songSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.next
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SongApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  test("get() should GET /v1/songs/{id} and return detail when 200") {
    val detail = Arb.songDetail(id = Arb.constant(SongId(1L))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req: HttpRequestData ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs/1"
      respond(
        responseJson, status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = SongApiImpl(client)
    val got = api.get(SongIdDto(1L))
    got shouldBe detail
  }

  test("list() should GET /v1/songs and return empty list") {
    val client = httpClientWith { req: HttpRequestData ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs"
      respond(
        json.encodeToString(emptyList<SongSummaryDto>()), status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val api = SongApiImpl(client)
    val list = api.list()
    list shouldBe emptyList()
  }

  test("list() should send query params and parse list of summaries") {
    val s1 = Arb.songSummary(id = Arb.constant(SongId(1L))).next().toDto()
    val s2 = Arb.songSummary(id = Arb.constant(SongId(2L))).next().toDto()
    val responseJson = json.encodeToString(listOf(s1, s2))

    val client = httpClientWith { req: HttpRequestData ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs"
      // Query parameters
      req.url.parameters["bookId"] shouldBe "book-1"
      req.url.parameters["minNumber"] shouldBe "5"
      req.url.parameters["maxNumber"] shouldBe "10"
      req.url.parameters["sort"] shouldBe "number"
      respond(
        responseJson, status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val api = SongApiImpl(client)
    val got = api.list(bookId = BookIdDto.parse("book-1"), minNumber = 5, maxNumber = 10, sort = SongSortDto.ByNumber)
    got shouldBe listOf(s1, s2)
  }

  test("get() should return null on 404") {
    val client = httpClientWith { req: HttpRequestData ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs/999"
      respond(
        json.encodeToString(ErrorDto.resourceNotFound(resourceType = io.github.alelk.pws.api.contract.core.ResourceTypeDto.SONG, resourceId = SongIdDto(999))),
        status = HttpStatusCode.NotFound,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val api = SongApiImpl(client)
    val got = api.get(SongIdDto(999))
    got shouldBe null
  }

  test("create() should POST /v1/songs and return ResourceCreateResult.Success") {
    val lyricDto = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "Verse 1\nChorus")))
    val createReq = SongCreateRequestDto(
      id = SongIdDto(1L),
      locale = LocaleDto("en"),
      name = "New Song",
      lyric = lyricDto,
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false,
      numbersInBook = emptyList()
    )
    val created = SongDetailDto(
      id = SongIdDto(1L),
      version = VersionDto("1.0"),
      locale = LocaleDto("en"),
      name = "New Song",
      lyric = lyricDto,
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false
    )
    val responseJson = json.encodeToString(created)

    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Post
      request.url.encodedPath shouldBe "/v1/songs"
      respond(
        responseJson, status = HttpStatusCode.Created,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = SongApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created.id)
  }

  test("create() should map 409 to AlreadyExists") {
    val req = SongCreateRequestDto(
      id = SongIdDto(5L),
      locale = LocaleDto("en"),
      name = "Dup",
      lyric = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "text"))),
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false,
      numbersInBook = emptyList()
    )
    val error = ErrorDto(code = io.github.alelk.pws.api.contract.core.error.ErrorCodes.ALREADY_EXISTS, message = "exists")
    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Post
      request.url.encodedPath shouldBe "/v1/songs"
      respond(json.encodeToString(error), status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = SongApiImpl(client)
    val got = api.create(req)
    got shouldBe ResourceCreateResult.AlreadyExists(req.id)
  }

  test("create() should map 400 to ValidationError") {
    val req = SongCreateRequestDto(
      id = SongIdDto(6L),
      locale = LocaleDto("en"),
      name = "Bad",
      lyric = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "text"))),
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false,
      numbersInBook = emptyList()
    )
    val error = ErrorDto(code = io.github.alelk.pws.api.contract.core.error.ErrorCodes.VALIDATION_ERROR, message = "bad")
    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Post
      request.url.encodedPath shouldBe "/v1/songs"
      respond(json.encodeToString(error), status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = SongApiImpl(client)
    val got = api.create(req)
    got shouldBe ResourceCreateResult.ValidationError("bad")
  }

  test("update() should PATCH /v1/songs/{id} and return Success") {
    val req = SongUpdateRequestDto(
      id = SongIdDto(10L),
      locale = null,
      name = "Updated",
      lyric = null,
      author = null,
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      expectedVersion = null
    )

    // Body doesn't matter, we just need a 200 with JSON parseable as SongIdDto
    val responseJson = json.encodeToString(SongIdDto(10L))

    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Patch
      request.url.encodedPath shouldBe "/v1/songs/10"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = SongApiImpl(client)
    val got = api.update(req)
    got shouldBe ResourceUpdateResult.Success(req.id)
  }

  test("update() should map 404 to NotFound") {
    val req = SongUpdateRequestDto(
      id = SongIdDto(11L),
      name = "Updated",
      locale = null,
      lyric = null,
      author = null,
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      expectedVersion = null
    )
    val error = ErrorDto(code = io.github.alelk.pws.api.contract.core.error.ErrorCodes.RESOURCE_NOT_FOUND, message = "not found")
    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Patch
      request.url.encodedPath shouldBe "/v1/songs/11"
      respond(json.encodeToString(error), status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = SongApiImpl(client)
    val got = api.update(req)
    got shouldBe ResourceUpdateResult.NotFound(req.id)
  }

  test("update() should map 400 to ValidationError") {
    val req = SongUpdateRequestDto(
      id = SongIdDto(12L),
      name = "Bad",
      locale = null,
      lyric = null,
      author = null,
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      expectedVersion = null
    )
    val error = ErrorDto(code = io.github.alelk.pws.api.contract.core.error.ErrorCodes.VALIDATION_ERROR, message = "bad")
    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Patch
      request.url.encodedPath shouldBe "/v1/songs/12"
      respond(json.encodeToString(error), status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = SongApiImpl(client)
    val got = api.update(req)
    got shouldBe ResourceUpdateResult.ValidationError("bad")
  }
})
