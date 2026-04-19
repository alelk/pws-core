package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.core.ColorDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceAlreadyExists
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.core.ids.TagIdDto
import io.github.alelk.pws.api.contract.tag.TagCreateRequestDto
import io.github.alelk.pws.api.contract.tag.TagDetailDto
import io.github.alelk.pws.api.contract.tag.TagSummaryDto
import io.github.alelk.pws.api.contract.tag.TagUpdateRequestDto
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
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
    val tag1 = TagSummaryDto.Predefined(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), edited = false)
    val tag2 = TagSummaryDto.Custom(TagIdDto("mytag"), "My Tag", priority = 5, ColorDto("#00FF00"))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags"
      respond(json.encodeToString<List<TagSummaryDto>>(listOf(tag1, tag2)), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).list() shouldBe listOf(tag1, tag2)
  }

  // --- get ---

  test("get(id) should GET /v1/user/tags/{id} and return parsed detail") {
    val detail = TagDetailDto.Custom(TagIdDto("mytag"), "My Tag", priority = 1, ColorDto("#00FF00"), songCount = 2)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond(json.encodeToString<TagDetailDto>(detail), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).get(TagIdDto("mytag")) shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, TagIdDto("missing")))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    UserTagApiImpl(client).get(TagIdDto("missing")) shouldBe null
  }

  // --- create ---

  test("create() should POST /v1/user/tags and return Either.Right") {
    val createReq = TagCreateRequestDto(id = TagIdDto("newtag"), name = "New Tag", color = ColorDto("#0000FF"), priority = 5)
    val created = TagDetailDto.Custom(TagIdDto("newtag"), "New Tag", priority = 5, ColorDto("#0000FF"), songCount = 0)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/tags"
      respond(json.encodeToString<TagDetailDto>(created), status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).create(createReq) shouldBe Either.Right(createReq.id)
  }

  test("create() should return Either.Left(AlreadyExists) when 409") {
    val createReq = TagCreateRequestDto(id = TagIdDto("existing"), name = "Existing", color = ColorDto("#FF0000"))
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.TAG, TagIdDto("existing")))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    UserTagApiImpl(client).create(createReq).shouldBeInstanceOf<Either.Left<CreateError.AlreadyExists>>()
  }

  // --- update ---

  test("update() should PUT /v1/user/tags/{id} and return Either.Right for custom tag") {
    val tagId = TagIdDto("mytag")
    val updated = TagDetailDto.Custom(tagId, "Renamed", priority = 1, ColorDto("#00FF00"), songCount = 2)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond(json.encodeToString<TagDetailDto>(updated), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).update(tagId, TagUpdateRequestDto(name = "Renamed")) shouldBe Either.Right(tagId)
  }

  test("update() should PUT /v1/user/tags/{id} and return Either.Right for predefined tag override") {
    val tagId = TagIdDto("worship")
    val updated = TagDetailDto.Predefined(tagId, "Worship", priority = 1, ColorDto("#AABBCC"), edited = true, songCount = 10)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/user/tags/worship"
      respond(json.encodeToString<TagDetailDto>(updated), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).update(tagId, TagUpdateRequestDto(color = ColorDto("#AABBCC"))) shouldBe Either.Right(tagId)
  }

  test("update() should return Either.Left(NotFound) when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    UserTagApiImpl(client).update(tagId, TagUpdateRequestDto(name = "Test")) shouldBe Either.Left(UpdateError.NotFound)
  }

  // --- delete ---

  test("delete() should DELETE /v1/user/tags/{id} and return Either.Right") {
    val tagId = TagIdDto("mytag")
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/tags/mytag"
      respond("", status = HttpStatusCode.NoContent)
    }
    UserTagApiImpl(client).delete(tagId) shouldBe Either.Right(tagId)
  }

  test("delete() should return Either.Left(NotFound) when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    UserTagApiImpl(client).delete(tagId) shouldBe Either.Left(DeleteError.NotFound)
  }

  // --- listSongs ---

  test("listSongs(id) should GET /v1/user/tags/{id}/songs and return song IDs") {
    val songIds = listOf(SongIdDto(1L), SongIdDto(2L))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs"
      respond(json.encodeToString(songIds), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    UserTagApiImpl(client).listSongs(TagIdDto("mytag")) shouldBe songIds
  }

  // --- addSongTag ---

  test("addSongTag() should POST and return Either.Right") {
    val tagId = TagIdDto("mytag")
    val songId = SongIdDto(1L)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs/1"
      respond("", status = HttpStatusCode.Created)
    }
    UserTagApiImpl(client).addSongTag(tagId, songId) shouldBe Either.Right(Unit)
  }

  // --- removeSongTag ---

  test("removeSongTag() should DELETE and return Either.Right") {
    val tagId = TagIdDto("mytag")
    val songId = SongIdDto(1L)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/user/tags/mytag/songs/1"
      respond("", status = HttpStatusCode.NoContent)
    }
    UserTagApiImpl(client).removeSongTag(tagId, songId) shouldBe Either.Right(Unit)
  }
})
