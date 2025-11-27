package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.core.LocaleDto
import io.github.alelk.pws.api.contract.core.PersonDto
import io.github.alelk.pws.api.contract.core.VersionDto
import io.github.alelk.pws.api.contract.core.ids.SongIdDto
import io.github.alelk.pws.api.contract.song.*
import io.github.alelk.pws.api.mapping.song.toDto
import io.github.alelk.pws.domain.core.ids.SongId
import io.github.alelk.pws.domain.song.model.songDetail
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

class SongApiTest : FunSpec({

  val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

  test("get() should return detail or null") {
    val detail = Arb.songDetail(id = Arb.constant(SongId(1L))).next().toDto()
    val responseJson = json.encodeToString(detail)

    val engine = MockEngine { _ ->
      respond(
        responseJson, status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", listOf(ContentType.Application.Json.toString()))
      )
    }
    val client = HttpClient(engine) { install(Resources); install(ContentNegotiation) { json(json) } }

    val api = SongApiImpl(client)
    val got = api.get(SongIdDto(1L))
    got shouldBe detail
  }

  test("create() should POST and return created detail") {
    val lyricDto = LyricDto(listOf(LyricPartDto.Verse(setOf(1), "Verse 1\nChorus")))
    val createReq = SongCreateRequestDto(
      id = SongIdDto(1L),
      locale = LocaleDto("en"),
      name = "New Song",
      lyric = lyricDto,
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false,
      numbersInBook = emptyList()
    )
    val created = SongDetailDto(
      id = SongIdDto(1L),
      version = VersionDto("1.0"),
      locale = LocaleDto("en"),
      name = "New Song",
      lyric = lyricDto,
      author = PersonDto("Author"),
      translator = null,
      composer = null,
      tonalities = null,
      year = null,
      bibleRef = null,
      edited = false
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

    val api = SongApiImpl(client)
    val got = api.create(createReq)
    got shouldBe ResourceCreateResult.Success(created.id)
  }
})
