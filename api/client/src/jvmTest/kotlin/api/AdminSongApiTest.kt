package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.ResourceTypeDto
import io.github.alelk.pws.api.contract.core.error.ErrorCodes
import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.github.alelk.pws.api.contract.core.error.resourceAlreadyExists
import io.github.alelk.pws.api.contract.core.error.resourceNotFound
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.LyricDto
import io.github.alelk.pws.api.contract.song.LyricPartDto
import io.github.alelk.pws.api.contract.song.SongCreateRequestDto
import io.github.alelk.pws.api.contract.song.SongSummaryDto
import io.github.alelk.pws.api.contract.song.SongUpdateRequestDto
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
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Tests for AdminSongApi (admin write operations).
 */
class AdminSongApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  fun httpClientWith(handler: MockRequestHandler): HttpClient =
    HttpClient(MockEngine { req -> handler(req) }) {
      install(Resources)
      install(ContentNegotiation) { json(json) }
    }

  // --- list ---

  test("list() should GET /v1/admin/songs and return parsed song summaries") {
    val song1 = Arb.songSummary(id = Arb.constant(SongId(1L))).next().toDto()
    val song2 = Arb.songSummary(id = Arb.constant(SongId(2L))).next().toDto()
    val responseJson = json.encodeToString(listOf(song1, song2))

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/songs"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val res = api.list()
    res shouldBe listOf(song1, song2)
  }

  // --- get ---

  test("get(id) should GET /v1/admin/songs/{id} and return parsed detail") {
    val detail = Arb.songDetail(id = Arb.constant(SongId(1L))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Get
      req.url.encodedPath shouldBe "/v1/admin/songs/1"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val res = api.get(SongIdDto(1L))
    res shouldBe detail
  }

  test("get(id) should return null when 404") {
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG, SongIdDto(999L)))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    api.get(SongIdDto(999L)) shouldBe null
  }

  // --- create ---

  test("create() should POST /v1/admin/songs and return ResourceCreateResult.Success") {
    val createReq = SongCreateRequestDto(
      id = SongIdDto(1L),
      locale = LocaleDto("en"),
      name = "New Song",
      lyric = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "Line 1")))
    )
    val created = SongIdDto(1L)
    val responseJson = json.encodeToString(created)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Post
      req.url.encodedPath shouldBe "/v1/admin/songs"
      respond(responseJson, status = HttpStatusCode.Created, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created)
  }

  test("create() should return AlreadyExists when 409") {
    val createReq = SongCreateRequestDto(
      id = SongIdDto(1L),
      locale = LocaleDto("en"),
      name = "Existing",
      lyric = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "Line 1")))
    )
    val errorJson = json.encodeToString(ErrorDto.resourceAlreadyExists(ResourceTypeDto.SONG, SongIdDto(1L)))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.Conflict, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.AlreadyExists(createReq.id)
  }

  test("create() should return ValidationError when 400") {
    val createReq = SongCreateRequestDto(
      id = SongIdDto(1L),
      locale = LocaleDto("en"),
      name = "",
      lyric = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "Line 1")))
    )
    val errorJson = json.encodeToString(ErrorDto(ErrorCodes.VALIDATION_ERROR, "Name is required"))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.BadRequest, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.ValidationError("Name is required")
  }

  // --- update ---

  test("update() should PATCH /v1/admin/songs/{id} and return Success") {
    val songId = SongIdDto(1L)
    val updateReq = SongUpdateRequestDto(id = songId, name = "Renamed")
    val responseJson = json.encodeToString(songId)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Patch
      req.url.encodedPath shouldBe "/v1/admin/songs/1"
      respond(responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.update(songId, updateReq)
    got shouldBe ResourceUpdateResult.Success(songId)
  }

  test("update() should return NotFound when 404") {
    val songId = SongIdDto(999L)
    val updateReq = SongUpdateRequestDto(id = songId, name = "Renamed")
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG, songId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.update(songId, updateReq)
    got shouldBe ResourceUpdateResult.NotFound(songId)
  }

  // --- delete ---

  test("delete() should DELETE /v1/admin/songs/{id} and return Success") {
    val songId = SongIdDto(1L)

    val client = httpClientWith { req ->
      req.method shouldBe HttpMethod.Delete
      req.url.encodedPath shouldBe "/v1/admin/songs/1"
      respond("", status = HttpStatusCode.NoContent, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.delete(songId)
    got shouldBe ResourceDeleteResult.Success(songId)
  }

  test("delete() should return NotFound when 404") {
    val songId = SongIdDto(999L)
    val errorJson = json.encodeToString(ErrorDto.resourceNotFound(ResourceTypeDto.SONG, songId))

    val client = httpClientWith {
      respond(errorJson, status = HttpStatusCode.NotFound, headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString())))
    }

    val api = AdminSongApiImpl(client)
    val got = api.delete(songId)
    got shouldBe ResourceDeleteResult.NotFound(songId)
  }
})

