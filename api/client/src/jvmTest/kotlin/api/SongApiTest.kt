package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.SongSortDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Tests for public read-only SongApi.
 * For admin write operations tests, see AdminSongApiTest.
 */
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

  test("get() should return null when 404 with RESOURCE_NOT_FOUND code") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG, SongIdDto(999L)))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = SongApiImpl(client)
    api.get(SongIdDto(999L)) shouldBe null
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

  test("list() should GET /v1/songs and return list of songs") {
    val song1 = Arb.songSummary(id = Arb.constant(SongId(1L))).next().toDto()
    val song2 = Arb.songSummary(id = Arb.constant(SongId(2L))).next().toDto()
    val responseJson = json.encodeToString(listOf(song1, song2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = SongApiImpl(client)
    val got = api.list()
    got shouldBe listOf(song1, song2)
  }

  test("list() should pass query params (bookId, minNumber, maxNumber, sort)") {
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/songs"
      req.url.parameters["bookId"] shouldBe "my-book"
      req.url.parameters["minNumber"] shouldBe "1"
      req.url.parameters["maxNumber"] shouldBe "100"
      req.url.parameters["sort"] shouldBe "name"
      respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    val api = SongApiImpl(client)
    api.list(
      bookId = BookIdDto("my-book"),
      minNumber = 1,
      maxNumber = 100,
      sort = SongSortDto.ByName
    )
  }
})
