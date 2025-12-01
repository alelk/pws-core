package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSortDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.songnumber.ReplaceAllBookSongNumbersResult
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.book.model.bookDetail
import io.github.alelk.pws.domain.book.model.bookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.songSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BookApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  test("list() should GET /v1/books and return parsed book summaries") {
    val book1 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val book2 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-two"))).next().toDto()
    val responseJson = json.encodeToString(listOf(book1, book2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      // Ktor Resources builds paths under /v1/books
      req.url.encodedPath shouldBe "/v1/books"
      respond(
        responseJson,
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = BookApiImpl(client)
    val res = api.list(locale = null, enabled = null, minPriority = null, sort = null)
    res shouldBe listOf(book1, book2)
  }

  test("list() should pass query params (locale, enabled, minPriority, sort)") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books"
      req.url.parameters["locale"] shouldBe "en"
      req.url.parameters["enabled"] shouldBe "true"
      req.url.parameters["minPriority"] shouldBe "3"
      req.url.parameters["sort"] shouldBe "priority"
      respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = BookApiImpl(client)
    val res = api.list(locale = LocaleDto("en"), enabled = true, minPriority = 3, sort = BookSortDto.ByPriority)
    res shouldBe emptyList()
  }

  test("get() should return detail when 200") {
    val detail = Arb.bookDetail(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books/book-one"
      respond(
        responseJson, status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = BookApiImpl(client)
    val got = api.get(BookIdDto("book-one"))
    got shouldBe detail
  }

  test("get() should return null on 404") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books/missing"
      respond(json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.BOOK, BookIdDto("missing"))), status = HttpStatusCode.NotFound)
    }
    val api = BookApiImpl(client)
    val got = api.get(BookIdDto("missing"))
    got shouldBe null
  }

  test("create() should POST /v1/books and return ResourceCreateResult.Success") {
    val createReq = BookCreateRequestDto(id = BookIdDto("new-book"), locale = LocaleDto("en"), name = "New", displayShortName = "N", displayName = "New Book")
    val created = BookDetailDto(
      id = BookIdDto("new-book"),
      version = VersionDto("1.0"),
      locale = LocaleDto("en"),
      name = "New",
      displayShortName = "N",
      displayName = "New Book",
      releaseDate = null,
      authors = null,
      creators = null,
      reviewers = null,
      editors = null,
      description = null,
      preface = null,
      firstSongNumberId = null,
      countSongs = 0,
      enabled = true,
      priority = 0
    )
    val responseJson = json.encodeToString(created)

    val client = httpClientWith { request ->
      request.method shouldBe HttpMethod.Post
      request.url.encodedPath shouldBe "/v1/books"
      respond(
        responseJson, status = HttpStatusCode.Created,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = BookApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created.id)
  }

  test("listBookSongs() should GET /v1/books/{id}/songs and return map of songs") {
    val song1 = Arb.songSummary(id = Arb.constant(SongId(1L))).next().toDto()
    val song2 = Arb.songSummary(id = Arb.constant(SongId(2L))).next().toDto()
    val map = mapOf(1 to song1, 2 to song2)
    val responseJson = json.encodeToString(map)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books/book-one/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = BookApiImpl(client)
    val got = api.listBookSongs(BookIdDto("book-one"))
    got shouldBe map
  }

  test("createBookSongs() should POST /v1/books/{id}/songs and return Success with provided items") {
    val bookId = BookIdDto("book-one")
    val links = listOf(SongNumberLinkDto(SongId(1).toDto(), 10), SongNumberLinkDto(SongId(2).toDto(), 20))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/books/${bookId.value}/songs"
      // API returns 201 with echo body (we don't rely on echo, BaseResourceApi just needs 2xx + parse)
      respond(
        Json { }.encodeToString(links),
        status = HttpStatusCode.Created,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }

    val api = BookApiImpl(client)
    val got = api.createBookSongs(bookId, links)
    when (got) {
      is ResourceBatchCreateResult.Success -> got.resources shouldContainExactlyInAnyOrder links
      else -> error("Expected Success, got $got")
    }
  }

  test("replaceBookSongs() should PUT /v1/books/{id}/songs and return parsed result") {
    val bookId = BookIdDto("book-one")
    val request = listOf(SongNumberLinkDto(SongId(1).toDto(), 1), SongNumberLinkDto(SongId(2).toDto(), 2))
    val result = ReplaceAllBookSongNumbersResult(
      created = listOf(SongNumberLinkDto(SongId(3).toDto(), 3)),
      updated = listOf(SongNumberLinkDto(SongId(2).toDto(), 2)),
      unchanged = emptyList(),
      deleted = listOf(SongNumberLinkDto(SongId(9).toDto(), 9)),
    )

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/books/${bookId.value}/songs"
      respond(json.encodeToString(result), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = BookApiImpl(client)
    val got = api.replaceBookSongs(bookId, request)
    got shouldBe result
  }

  test("update() should PATCH /v1/books/{id} and return ResourceUpdateResult.Success") {
    val bookId = BookIdDto("book-one")
    val update = io.github.alelk.pws.api.contract.book.BookUpdateRequestDto(name = "Renamed")
    val echo = BookDetailDto(
      id = bookId,
      version = VersionDto("1.0"),
      locale = LocaleDto("en"),
      name = "Renamed",
      displayShortName = "N",
      displayName = "New Book",
      releaseDate = null,
      authors = null,
      creators = null,
      reviewers = null,
      editors = null,
      description = null,
      preface = null,
      firstSongNumberId = null,
      countSongs = 0,
      enabled = true,
      priority = 0
    )

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Patch
      req.url.encodedPath shouldBe "/v1/books/${bookId.value}"
      respond(json.encodeToString(echo), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = BookApiImpl(client)
    val got = api.update(bookId, update)
    got shouldBe ResourceUpdateResult.Success(bookId)
  }
})
