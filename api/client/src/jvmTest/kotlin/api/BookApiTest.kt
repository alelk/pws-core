package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.book.songnumber.SongNumberLinkDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.core.toDto
import io.github.alelk.pws.domain.book.model.bookDetail
import io.github.alelk.pws.domain.book.model.bookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
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
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Tests for public read-only BookApi.
 * For admin write operations tests, see AdminBookApiTest.
 */
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
      req.url.parameters["locale"] shouldBe "ru"
      req.url.parameters["enabled"] shouldBe "true"
      req.url.parameters["minPriority"] shouldBe "5"
      req.url.parameters["sort"] shouldBe "name"
      respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = BookApiImpl(client)
    api.list(
      locale = io.github.alelk.pws.api.contract.core.LocaleDto("ru"),
      enabled = true,
      minPriority = 5,
      sort = io.github.alelk.pws.api.contract.book.BookSortDto.ByName
    )
  }

  test("get(id) should GET /v1/books/{id} and return parsed detail") {
    val detail = Arb.bookDetail(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books/book-one"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = BookApiImpl(client)
    val res = api.get(BookIdDto("book-one"))
    res shouldBe detail
  }

  test("get(id) should return null when 404 with RESOURCE_NOT_FOUND code") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.BOOK, BookIdDto("missing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = BookApiImpl(client)
    api.get(BookIdDto("missing")) shouldBe null
  }

  test("listBookSongs() should GET /v1/books/{id}/songs and return list of song number links") {
    val links = listOf(
      SongNumberLinkDto(SongId(1).toDto(), 1),
      SongNumberLinkDto(SongId(2).toDto(), 2)
    )
    val responseJson = json.encodeToString(links)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/books/book-one/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = BookApiImpl(client)
    val got = api.listBookSongs(BookIdDto("book-one"))
    got shouldBe links
  }
})
