package io.github.alelk.pws.api.client.api

import arrow.core.Either
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceAlreadyExists
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.songreference.ReplaceAllSongReferencesResultDto
import io.github.alelk.pws.api.contract.songreference.SongRefReasonDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceCreateRequestDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceDto
import io.github.alelk.pws.api.contract.songreference.SongReferenceUpdateRequestDto
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
 * Tests for AdminSongReferenceApi (admin operations for song-to-song references).
 */
class AdminSongReferenceApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/admin/songs/{songId}/references and return parsed references") {
    val songId = SongIdDto(1L)
    val refs = listOf(SongReferenceDto(songId, SongIdDto(2L), SongRefReasonDto.VARIATION, 80, 1), SongReferenceDto(songId, SongIdDto(3L), SongRefReasonDto.VARIATION, 60, 2))
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/songs/1/references"
      respond(json.encodeToString(refs), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    AdminSongReferenceApiImpl(client).list(songId) shouldBe refs
  }

  // --- create ---

  test("create() should POST and return Either.Right") {
    val songId = SongIdDto(1L)
    val createReq = SongReferenceCreateRequestDto(refSongId = SongIdDto(2L), reason = SongRefReasonDto.VARIATION, volume = 80, priority = 1)
    val createdRef = SongReferenceDto(songId, SongIdDto(2L), SongRefReasonDto.VARIATION, 80, 1)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/admin/songs/1/references"
      respond(json.encodeToString(createdRef), status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    AdminSongReferenceApiImpl(client).create(songId, createReq) shouldBe Either.Right(createdRef)
  }

  test("create() should return Either.Left(AlreadyExists) when 409") {
    val songId = SongIdDto(1L)
    val createReq = SongReferenceCreateRequestDto(refSongId = SongIdDto(2L), reason = SongRefReasonDto.VARIATION, volume = 80)
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.SONG_REFERENCE, SongReferenceDto(songId, SongIdDto(2L), SongRefReasonDto.VARIATION, 80, 0)))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    AdminSongReferenceApiImpl(client).create(songId, createReq).shouldBeInstanceOf<Either.Left<CreateError.AlreadyExists>>()
  }

  test("create() should return Either.Left(ValidationError) when 400") {
    val songId = SongIdDto(1L)
    val createReq = SongReferenceCreateRequestDto(refSongId = SongIdDto(2L), reason = SongRefReasonDto.VARIATION, volume = 80)
    val errorJson = json.encodeToString(ErrorDto(ErrorCodes.VALIDATION_ERROR, "Invalid reference"))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    AdminSongReferenceApiImpl(client).create(songId, createReq) shouldBe Either.Left(CreateError.ValidationError("Invalid reference"))
  }

  // --- update ---

  test("update() should PATCH and return Either.Right") {
    val songId = SongIdDto(1L)
    val refSongId = SongIdDto(2L)
    val updateReq = SongReferenceUpdateRequestDto(reason = SongRefReasonDto.VARIATION, volume = 90, priority = 5)
    val updatedRef = SongReferenceDto(songId, refSongId, SongRefReasonDto.VARIATION, 90, 5)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Patch
      req.url.encodedPath shouldBe "/v1/admin/songs/1/references/2"
      respond(json.encodeToString(updatedRef), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    AdminSongReferenceApiImpl(client).update(songId, refSongId, updateReq) shouldBe Either.Right(updatedRef)
  }

  test("update() should return Either.Left(NotFound) when 404") {
    val songId = SongIdDto(1L)
    val refSongId = SongIdDto(999L)
    val updateReq = SongReferenceUpdateRequestDto(volume = 90, reason = SongRefReasonDto.VARIATION)
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG_REFERENCE, refSongId))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    AdminSongReferenceApiImpl(client).update(songId, refSongId, updateReq) shouldBe Either.Left(UpdateError.NotFound)
  }

  // --- delete ---

  test("delete() should DELETE and return Either.Right") {
    val songId = SongIdDto(1L)
    val refSongId = SongIdDto(2L)
    val expectedRef = SongReferenceDto(songId, refSongId, SongRefReasonDto.VARIATION, 0, 0)
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/admin/songs/1/references/2"
      respond("", status = HttpStatusCode.NoContent, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    AdminSongReferenceApiImpl(client).delete(songId, refSongId) shouldBe Either.Right(expectedRef)
  }

  test("delete() should return Either.Left(NotFound) when 404") {
    val songId = SongIdDto(1L)
    val refSongId = SongIdDto(999L)
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG_REFERENCE, refSongId))
    val client = httpClientWith { respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))) }
    AdminSongReferenceApiImpl(client).delete(songId, refSongId) shouldBe Either.Left(DeleteError.NotFound)
  }

  // --- replace ---

  test("replace() should PUT and return result") {
    val songId = SongIdDto(1L)
    val references = listOf(SongReferenceDto(songId, SongIdDto(2L), SongRefReasonDto.VARIATION, 80, 1))
    val result = ReplaceAllSongReferencesResultDto(created = references, updated = emptyList(), unchanged = emptyList(), deleted = emptyList())
    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Put
      req.url.encodedPath shouldBe "/v1/admin/songs/1/references"
      respond(json.encodeToString(result), status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }
    AdminSongReferenceApiImpl(client).replace(songId, references) shouldBe result
  }
})
