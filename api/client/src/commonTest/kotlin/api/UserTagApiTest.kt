package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceAlreadyExists
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.api.contract.usertag.UserTagOverrideDto
import io.github.alelk.pws.api.contract.usertag.UserTagOverrideUpdateRequestDto
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
 * Tests for UserTagApi (user tag operations).
 */
class UserTagApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/user/tags and return parsed tag summaries") {
    val tag1 = TagSummaryDto(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), predefined = true)
    val tag2 = TagSummaryDto(TagIdDto("mytag"), "My Tag", priority = 5, ColorDto("#00FF00"), predefined = false)
    val responseJson = json.encodeToString(listOf(tag1, tag2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val res = api.list()
    res shouldBe listOf(tag1, tag2)
  }

  // --- get ---

  test("get(id) should GET /v1/user/tags/{id} and return parsed detail") {
    val detail = TagDetailDto(TagIdDto("mytag"), "My Tag", priority = 1, ColorDto("#00FF00"), predefined = false, songCount = 2)
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val res = api.get(TagIdDto("mytag"))
    res shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, TagIdDto("missing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    api.get(TagIdDto("missing")) shouldBe null
  }

  // --- create ---

  test("create() should POST /v1/user/tags and return ResourceCreateResult.Success") {
    val createReq = TagCreateRequestDto(id = TagIdDto("newtag"), name = "New Tag", color = ColorDto("#0000FF"), priority = 5)
    val created = TagDetailDto(TagIdDto("newtag"), "New Tag", priority = 5, ColorDto("#0000FF"), predefined = false, songCount = 0)
    val responseJson = json.encodeToString(created)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/tags"
      respond(responseJson, status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(createReq.id)
  }

  test("create() should return AlreadyExists when 409") {
    val createReq = TagCreateRequestDto(id = TagIdDto("existing"), name = "Existing", color = ColorDto("#FF0000"))
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.TAG, TagIdDto("existing")))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.AlreadyExists(createReq.id)
  }

  // --- update ---

  test("update() should PATCH /v1/user/tags/{id} and return Success") {
    val tagId = TagIdDto("mytag")
    val updateReq = TagUpdateRequestDto(name = "Renamed")
    val updated = TagDetailDto(tagId, "Renamed", priority = 1, ColorDto("#00FF00"), predefined = false, songCount = 2)
    val responseJson = json.encodeToString(updated)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Patch
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.update(tagId, updateReq)
    got shouldBe ResourceUpdateResult.Success(tagId)
  }

  test("update() should return NotFound when 404") {
    val tagId = TagIdDto("missing")
    val updateReq = TagUpdateRequestDto(name = "Test")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.update(tagId, updateReq)
    got shouldBe ResourceUpdateResult.NotFound(tagId)
  }

  // --- delete ---

  test("delete() should DELETE /v1/user/tags/{id} and return Success") {
    val tagId = TagIdDto("mytag")

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond("", status = HttpStatusCode.NoContent)
    }

    val api = UserTagApiImpl(client)
    val got = api.delete(tagId)
    got shouldBe ResourceDeleteResult.Success(tagId)
  }

  test("delete() should return NotFound when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.delete(tagId)
    got shouldBe ResourceDeleteResult.NotFound(tagId)
  }

  // --- listSongs ---

  test("listSongs(id) should GET /v1/user/tags/{id}/songs and return song IDs") {
    val songIds = listOf(SongIdDto(1L), SongIdDto(2L))
    val responseJson = json.encodeToString(songIds)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val res = api.listSongs(TagIdDto("mytag"))
    res shouldBe songIds
  }

  // --- addSongTag ---

  test("addSongTag() should POST /v1/user/tags/{id}/songs/{songId} and return Success") {
    val tagId = TagIdDto("mytag")
    val songId = SongIdDto(1L)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs/1"
      respond("", status = HttpStatusCode.Created)
    }

    val api = UserTagApiImpl(client)
    val got = api.addSongTag(tagId, songId)
    got shouldBe ResourceCreateResult.Success(Unit)
  }

  // --- removeSongTag ---

  test("removeSongTag() should DELETE /v1/user/tags/{id}/songs/{songId} and return Success") {
    val tagId = TagIdDto("mytag")
    val songId = SongIdDto(1L)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs/1"
      respond("", status = HttpStatusCode.NoContent)
    }

    val api = UserTagApiImpl(client)
    val got = api.removeSongTag(tagId, songId)
    got shouldBe ResourceDeleteResult.Success(Unit)
  }

  // --- setOverride ---

  test("setOverride() should PUT /v1/user/tags/overrides/{id} and return override") {
    val tagId = TagIdDto("worship")
    val request = UserTagOverrideUpdateRequestDto(hidden = true, colorOverride = ColorDto("#FF00FF"))
    val response = UserTagOverrideDto(tagId = tagId, hidden = true, colorOverride = ColorDto("#FF00FF"))
    val responseJson = json.encodeToString(response)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/user/tags/overrides/worship"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.setOverride(tagId, request)
    got shouldBe response
  }

  // --- resetOverride ---

  test("resetOverride() should DELETE /v1/user/tags/overrides/{id} and return Success") {
    val tagId = TagIdDto("worship")

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/tags/overrides/worship"
      respond("", status = HttpStatusCode.NoContent)
    }

    val api = UserTagApiImpl(client)
    val got = api.resetOverride(tagId)
    got shouldBe ResourceDeleteResult.Success(tagId)
  }

  test("resetOverride() should return NotFound when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = UserTagApiImpl(client)
    val got = api.resetOverride(tagId)
    got shouldBe ResourceDeleteResult.NotFound(tagId)
  }
})

