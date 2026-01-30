package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
 * Tests for TagApi (public read-only operations).
 */
class TagApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/tags and return parsed tag summaries") {
    val tag1 = TagSummaryDto(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), predefined = true)
    val tag2 = TagSummaryDto(TagIdDto("praise"), "Praise", priority = 2, ColorDto("#00FF00"), predefined = true)
    val responseJson = json.encodeToString(listOf(tag1, tag2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/tags"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = TagApiImpl(client)
    val res = api.list()
    res shouldBe listOf(tag1, tag2)
  }

  // --- get ---

  test("get(id) should GET /v1/tags/{id} and return parsed detail") {
    val detail = TagDetailDto(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), predefined = true, songCount = 10)
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/tags/worship"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = TagApiImpl(client)
    val res = api.get(TagIdDto("worship"))
    res shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, TagIdDto("missing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = TagApiImpl(client)
    api.get(TagIdDto("missing")) shouldBe null
  }

  // --- listSongs ---

  test("listSongs(id) should GET /v1/tags/{id}/songs and return song IDs") {
    val songIds = listOf(SongIdDto(1L), SongIdDto(2L), SongIdDto(3L))
    val responseJson = json.encodeToString(songIds)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/tags/worship/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = TagApiImpl(client)
    val res = api.listSongs(TagIdDto("worship"))
    res shouldBe songIds
  }
})

