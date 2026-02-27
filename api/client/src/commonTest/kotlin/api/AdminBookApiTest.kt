package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookUpdateRequestDto
import io.github.alelk.pws.api.contract.book.songnumber.ReplaceAllBookSongNumbersResult
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceAlreadyExists
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.bookDetail
import io.github.alelk.pws.domain.book.model.bookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
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
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Tests for AdminBookApi (admin write operations).
 */
class AdminBookApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/admin/books and return parsed book summaries") {
    val book1 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val book2 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-two"))).next().toDto()
    val responseJson = json.encodeToString(listOf(book1, book2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/books"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val res = api.list()
    res shouldBe listOf(book1, book2)
  }

  // --- get ---

  test("get(id) should GET /v1/admin/books/{id} and return parsed detail") {
    val detail = Arb.bookDetail(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/books/book-one"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val res = api.get(BookIdDto("book-one"))
    res shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.BOOK, BookIdDto("missing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    api.get(BookIdDto("missing")) shouldBe null
  }

  // --- create ---

  test("create() should POST /v1/admin/books and return ResourceCreateResult.Success") {
    val createReq =
      BookCreateRequestDto(id = BookIdDto("new-book"), locales = listOf(LocaleDto("en")), name = "New", displayShortName = "N", displayName = "New Book")
    val created = BookIdDto("new-book")
    val responseJson = json.encodeToString(created)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/admin/books"
      respond(responseJson, status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created)
  }

  test("create() should return AlreadyExists when 409") {
    val createReq = BookCreateRequestDto(id = BookIdDto("existing"), locales = listOf(LocaleDto("en")), name = "Existing")
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.BOOK, BookIdDto("existing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.AlreadyExists(createReq.id)
  }

  test("create() should return ValidationError when 400") {
    val createReq = BookCreateRequestDto(id = BookIdDto("invalid"), locales = listOf(LocaleDto("en")), name = "")
    val errorJson = json.encodeToString(ErrorDto(ErrorCodes.VALIDATION_ERROR, "Name is required"))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.ValidationError("Name is required")
  }

  // --- update ---

  test("update() should PATCH /v1/admin/books/{id} and return Success") {
    val bookId = BookIdDto("book-one")
    val updateReq = BookUpdateRequestDto(name = "Renamed")
    val responseJson = json.encodeToString(bookId)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Patch
      req.url.encodedPath shouldBe "/v1/admin/books/${bookId.value}"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.update(bookId, updateReq)
    got shouldBe ResourceUpdateResult.Success(bookId)
  }

  test("update() should return NotFound when 404") {
    val bookId = BookIdDto("missing")
    val updateReq = BookUpdateRequestDto(name = "Renamed")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.BOOK, bookId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.update(bookId, updateReq)
    got shouldBe ResourceUpdateResult.NotFound(bookId)
  }

  // --- delete ---

  test("delete() should DELETE /v1/admin/books/{id} and return Success") {
    val bookId = BookIdDto("book-one")

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/admin/books/${bookId.value}"
      respond("", status = HttpStatusCode.NoContent, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.delete(bookId)
    got shouldBe ResourceDeleteResult.Success(bookId)
  }

  test("delete() should return NotFound when 404") {
    val bookId = BookIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.BOOK, bookId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.delete(bookId)
    got shouldBe ResourceDeleteResult.NotFound(bookId)
  }

  // --- listSongs ---

  test("listSongs() should GET /v1/admin/books/{id}/songs") {
    val bookId = BookIdDto("book-one")
    val links = listOf(SongNumberLinkDto(SongId(1).toDto(), 1), SongNumberLinkDto(SongId(2).toDto(), 2))
    val responseJson = json.encodeToString(links)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/books/${bookId.value}/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.listSongs(bookId)
    got shouldBe links
  }

  // --- addSongs ---

  test("addSongs() should POST /v1/admin/books/{id}/songs and return Success") {
    val bookId = BookIdDto("book-one")
    val links = listOf(SongNumberLinkDto(SongId(1).toDto(), 10), SongNumberLinkDto(SongId(2).toDto(), 20))
    val responseJson = json.encodeToString(links)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/admin/books/${bookId.value}/songs"
      respond(responseJson, status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.addSongs(bookId, links)
    when (got) {
      is ResourceBatchCreateResult.Success -> got.resources shouldContainExactlyInAnyOrder links
      else -> error("Expected Success, got $got")
    }
  }

  // --- replaceSongs ---

  test("replaceSongs() should PUT /v1/admin/books/{id}/songs and return result") {
    val bookId = BookIdDto("book-one")
    val request = listOf(SongNumberLinkDto(SongId(1).toDto(), 1), SongNumberLinkDto(SongId(2).toDto(), 2))
    val result = ReplaceAllBookSongNumbersResult(
      created = listOf(SongNumberLinkDto(SongId(3).toDto(), 3)),
      updated = listOf(SongNumberLinkDto(SongId(2).toDto(), 2)),
      unchanged = emptyList(),
      deleted = listOf(SongNumberLinkDto(SongId(9).toDto(), 9)),
    )
    val responseJson = json.encodeToString(result)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/admin/books/${bookId.value}/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminBookApiImpl(client)
    val got = api.replaceSongs(bookId, request)
    got shouldBe result
  }
})

