package io.github.alelk.pws.api.client.api

import arrow.core.Either
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
import io.github.alelk.pws.domain.core.error.CreateError
import io.github.alelk.pws.domain.core.error.DeleteError
import io.github.alelk.pws.domain.core.error.UpdateError
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Tests for AdminTagApi (admin write operations).
 */
class AdminTagApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/admin/tags and return parsed tag summaries") {
    val tag1 = TagSummaryDto.Predefined(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), edited = false)
    val tag2 = TagSummaryDto.Predefined(TagIdDto("praise"), "Praise", priority = 2, ColorDto("#00FF00"), edited = false)
    val responseJson = json.encodeToString<List<TagSummaryDto>>(listOf(tag1, tag2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/tags"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    AdminTagApiImpl(client).list() shouldBe listOf(tag1, tag2)
  }

  // --- get ---

  test("get(id) should GET /v1/admin/tags/{id} and return parsed detail") {
    val detail = TagDetailDto.Predefined(TagIdDto("worship"), "Worship", priority = 1, ColorDto("#FF0000"), edited = false, songCount = 10)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/tags/worship"
      respond(json.encodeToString<TagDetailDto>(detail), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    AdminTagApiImpl(client).get(TagIdDto("worship")) shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, TagIdDto("missing")))

    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }

    AdminTagApiImpl(client).get(TagIdDto("missing")) shouldBe null
  }

  // --- create ---

  test("create() should POST /v1/admin/tags and return Either.Right") {
    val createReq = TagCreateRequestDto(id = TagIdDto("newtag"), name = "New Tag", color = ColorDto("#0000FF"), priority = 5)
    val created = TagDetailDto.Predefined(TagIdDto("newtag"), "New Tag", priority = 5, ColorDto("#0000FF"), edited = false, songCount = 0)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/admin/tags"
      respond(json.encodeToString<TagDetailDto>(created), status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    AdminTagApiImpl(client).create(createReq) shouldBe Either.Right(createReq.id)
  }

  test("create() should return Either.Left(AlreadyExists) when 409") {
    val createReq = TagCreateRequestDto(id = TagIdDto("existing"), name = "Existing", color = ColorDto("#FF0000"))
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.TAG, TagIdDto("existing")))

    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }

    AdminTagApiImpl(client).create(createReq).shouldBeInstanceOf<Either.Left<CreateError.AlreadyExists>>()
  }

  test("create() should return Either.Left(ValidationError) when 400") {
    val createReq = TagCreateRequestDto(id = TagIdDto("invalid"), name = "", color = ColorDto("#FF0000"))
    val errorJson = json.encodeToString(ErrorDto(ErrorCodes.VALIDATION_ERROR, "Name is required"))

    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }

    AdminTagApiImpl(client).create(createReq) shouldBe Either.Left(CreateError.ValidationError("Name is required"))
  }

  // --- update ---

  test("update() should PUT /v1/admin/tags/{id} and return Either.Right") {
    val tagId = TagIdDto("worship")
    val updated = TagDetailDto.Predefined(tagId, "Renamed", priority = 1, ColorDto("#FF0000"), edited = false, songCount = 10)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/admin/tags/worship"
      respond(json.encodeToString<TagDetailDto>(updated), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    AdminTagApiImpl(client).update(tagId, TagUpdateRequestDto(name = "Renamed")) shouldBe Either.Right(tagId)
  }

  test("update() should return Either.Left(NotFound) when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))

    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }

    AdminTagApiImpl(client).update(tagId, TagUpdateRequestDto(name = "Test")) shouldBe Either.Left(UpdateError.NotFound)
  }

  // --- delete ---

  test("delete() should DELETE /v1/admin/tags/{id} and return Either.Right") {
    val tagId = TagIdDto("worship")

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/admin/tags/worship"
      respond("", status = HttpStatusCode.NoContent)
    }

    AdminTagApiImpl(client).delete(tagId) shouldBe Either.Right(tagId)
  }

  test("delete() should return Either.Left(NotFound) when 404") {
    val tagId = TagIdDto("missing")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.TAG, tagId))

    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }

    AdminTagApiImpl(client).delete(tagId) shouldBe Either.Left(DeleteError.NotFound)
  }

  // --- listSongs ---

  test("listSongs(id) should GET /v1/admin/tags/{id}/songs and return song IDs") {
    val songIds = listOf(SongIdDto(1L), SongIdDto(2L), SongIdDto(3L))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/tags/worship/songs"
      respond(json.encodeToString(songIds), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    AdminTagApiImpl(client).listSongs(TagIdDto("worship")) shouldBe songIds
  }
})
