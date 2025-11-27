package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.book.BookCreateRequestDto
import io.github.alelk.pws.api.contract.book.BookDetailDto
import io.github.alelk.pws.api.contract.book.BookSummaryDto
import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.mapping.book.toDto
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.book.model.bookDetail
import io.github.alelk.pws.domain.book.model.bookSummary
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.core.result.CreateResourceResult
import io.github.alelk.pws.domain.song.model.songSummary
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.next
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class BookApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  test("list() should return parsed book summaries") {
    val book1 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val book2 = Arb.bookSummary(id = Arb.constant(BookId.parse("book-two"))).next().toDto()

    val responseJson = json.encodeToString(listOf(book1, book2))

    val engine = MockEngine { _ ->
      respond(
        responseJson,
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val client = HttpClient(engine) { install(Resources); install(ContentNegotiation) { json(json) } }

    val api = BookApiImpl(client)
    val res = api.list(locale = null, enabled = null, minPriority = null, sort = null)
    res shouldBe listOf(book1, book2)
  }

  test("get() should return detail or null") {
    val detail = Arb.bookDetail(id = Arb.constant(BookId.parse("book-one"))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val engine = MockEngine { _ ->
      respond(
        responseJson, status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val client = HttpClient(engine) { install(Resources); install(ContentNegotiation) { json(json) } }

    val api = BookApiImpl(client)
    val got = api.get(BookIdDto("book-one"))
    got shouldBe detail
  }

  test("create() should POST and return created detail") {
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

    val engine = MockEngine { request ->
      if (request.method == HttpMethod.Post) {
        respond(
          responseJson, status = HttpStatusCode.Created,
          headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
        )
      } else respond("", status = HttpStatusCode.MethodNotAllowed)
    }
    val client = HttpClient(engine) { install(Resources); install(ContentNegotiation) { json(json) } }

    val api = BookApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created.id)
  }

  test("listBookSongs() should return map of songs") {
    val song1 = Arb.songSummary(id = Arb.constant(SongId(1L))).next().toDto()
    val song2 = Arb.songSummary(id = Arb.constant(SongId(2L))).next().toDto()
    val map = mapOf(1 to song1, 2 to song2)
    val responseJson = json.encodeToString(map)

    val engine = MockEngine { _ ->
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val client = HttpClient(engine) { install(Resources); install(ContentNegotiation) { json(json) } }

    val api = BookApiImpl(client)
    val got = api.listBookSongs(BookIdDto("book-one"))
    got shouldBe map
  }
})
