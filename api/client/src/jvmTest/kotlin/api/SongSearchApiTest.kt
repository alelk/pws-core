package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.ids.BookIdDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.MatchedFieldDto
import io.github.alelk.pws.api.contract.song.SearchTypeDto
import io.github.alelk.pws.api.contract.song.SongBookReferenceDto
import io.github.alelk.pws.api.contract.song.SongSearchResponseDto
import io.github.alelk.pws.api.contract.song.SongSearchResultDto
import io.github.alelk.pws.api.contract.song.SongSearchSuggestionDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Tests for SongApi search methods (search, suggestions).
 */
class SongSearchApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true; explicitNulls = false }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // Test fixtures
  fun testSuggestion(id: Long = 1, name: String = "Test Song") = SongSearchSuggestionDto(
    id = SongIdDto(id),
    name = name,
    bookReferences = listOf(
      SongBookReferenceDto(BookIdDto("hymnal"), "HYM", 101),
      SongBookReferenceDto(BookIdDto("psalms"), "PSA", 42)
    ),
    snippet = "Test snippet with <mark>match</mark>"
  )

  fun testSearchResult(id: Long = 1, name: String = "Test Song") = SongSearchResultDto(
    song = SongSummaryDto(
      id = SongIdDto(id),
      name = name,
      locale = LocaleDto("en"),
      version = VersionDto("1.0"),
      edited = false
    ),
    snippet = "Test snippet",
    rank = 0.5f,
    matchedFields = listOf(MatchedFieldDto.NAME, MatchedFieldDto.LYRIC)
  )

  // ==================== SUGGESTIONS TESTS ====================

  context("suggestions()") {

    test("should GET /v1/songs/search/suggestions and return parsed suggestions") {
      val suggestion1 = testSuggestion(1, "Amazing Grace")
      val suggestion2 = testSuggestion(2, "How Great Thou Art")
      val responseJson = json.encodeToString(listOf(suggestion1, suggestion2))

      val client = httpClientWith { req ->
        req.method shouldBe HttpMethod.Get
        req.url.encodedPath shouldBe "/v1/songs/search/suggestions"
        req.url.parameters["query"] shouldBe "grace"
        respond(
          responseJson,
          status = HttpStatusCode.OK,
          headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
        )
      }

      val api = SongApiImpl(client)
      val res = api.suggestions("grace")
      res shouldBe listOf(suggestion1, suggestion2)
    }

    test("should pass query parameter") {
      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "amazing grace"
        respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }
      val api = SongApiImpl(client)
      api.suggestions("amazing grace")
    }

    test("should pass bookId when provided") {
      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "test"
        req.url.parameters["bookId"] shouldBe "hymnal-1"
        respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }
      val api = SongApiImpl(client)
      api.suggestions("test", bookId = BookIdDto("hymnal-1"))
    }

    test("should pass limit when provided") {
      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "test"
        req.url.parameters["limit"] shouldBe "25"
        respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }
      val api = SongApiImpl(client)
      api.suggestions("test", limit = 25)
    }

    test("should not include optional params when null") {
      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "test"
        req.url.parameters.contains("bookId") shouldBe false
        req.url.parameters.contains("limit") shouldBe false
        respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }
      val api = SongApiImpl(client)
      api.suggestions("test")
    }

    test("should return empty list when no suggestions") {
      val client = httpClientWith { req ->
        respond("[]", status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }
      val api = SongApiImpl(client)
      val res = api.suggestions("nonexistent")
      res shouldBe emptyList()
    }
  }

  // ==================== SEARCH TESTS ====================

  context("search()") {

    test("should GET /v1/songs/search and return parsed response") {
      val result1 = testSearchResult(1, "Amazing Grace")
      val result2 = testSearchResult(2, "How Great Thou Art")
      val response = SongSearchResponseDto(
        results = listOf(result1, result2),
        totalCount = 2,
        hasMore = false
      )
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        req.method shouldBe HttpMethod.Get
        req.url.encodedPath shouldBe "/v1/songs/search"
        req.url.parameters["query"] shouldBe "grace"
        respond(
          responseJson,
          status = HttpStatusCode.OK,
          headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
        )
      }

      val api = SongApiImpl(client)
      val res = api.search("grace")
      res shouldBe response
    }

    test("should pass all query parameters") {
      val response = SongSearchResponseDto(emptyList(), 0, false)
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "test"
        req.url.parameters["type"] shouldBe "NAME"
        req.url.parameters["bookId"] shouldBe "hymnal-1"
        req.url.parameters["limit"] shouldBe "50"
        req.url.parameters["offset"] shouldBe "20"
        req.url.parameters["highlight"] shouldBe "false"
        respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }

      val api = SongApiImpl(client)
      api.search(
        query = "test",
        type = SearchTypeDto.NAME,
        bookId = BookIdDto("hymnal-1"),
        limit = 50,
        offset = 20,
        highlight = false
      )
    }

    test("should pass SearchType.LYRIC") {
      val response = SongSearchResponseDto(emptyList(), 0, false)
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        req.url.parameters["type"] shouldBe "LYRIC"
        respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }

      val api = SongApiImpl(client)
      api.search("test", type = SearchTypeDto.LYRIC)
    }

    test("should not include optional params when null") {
      val response = SongSearchResponseDto(emptyList(), 0, false)
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        req.url.parameters["query"] shouldBe "test"
        req.url.parameters.contains("type") shouldBe false
        req.url.parameters.contains("bookId") shouldBe false
        req.url.parameters.contains("limit") shouldBe false
        req.url.parameters.contains("offset") shouldBe false
        req.url.parameters.contains("highlight") shouldBe false
        respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }

      val api = SongApiImpl(client)
      api.search("test")
    }

    test("should return response with hasMore=true for paginated results") {
      val result = testSearchResult()
      val response = SongSearchResponseDto(
        results = listOf(result),
        totalCount = 100,
        hasMore = true
      )
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }

      val api = SongApiImpl(client)
      val res = api.search("test", limit = 1)
      res.hasMore shouldBe true
      res.totalCount shouldBe 100
    }

    test("should return empty results when no matches") {
      val response = SongSearchResponseDto(emptyList(), 0, false)
      val responseJson = json.encodeToString(response)

      val client = httpClientWith { req ->
        respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
      }

      val api = SongApiImpl(client)
      val res = api.search("nonexistent")
      res.results shouldBe emptyList()
      res.totalCount shouldBe 0
      res.hasMore shouldBe false
    }
  }
})
