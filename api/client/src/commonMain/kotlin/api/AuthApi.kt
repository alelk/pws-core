package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.auth.*
import io.github.alelk.pws.domain.auth.storage.TokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*

interface AuthApi {
  suspend fun register(email: String, password: String, username: String? = null): AuthResponseDto
  suspend fun login(email: String, password: String): AuthResponseDto
  suspend fun loginWithTelegram(initData: String): AuthResponseDto
  suspend fun getMe(): UserResponseDto?
  suspend fun linkTelegram(initData: String): UserResponseDto
}

internal class AuthApiImpl(
  client: HttpClient,
  val tokenStorage: TokenStorage? = null
) : BaseResourceApi(client), AuthApi {
  override suspend fun register(email: String, password: String, username: String?): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Register()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(RegisterRequestDto(email, password, username))
      }
    }.getOrThrow().also { tokenStorage?.saveToken(it.token) }

  override suspend fun login(email: String, password: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Login()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(LoginRequestDto(email, password))
      }
    }.getOrThrow().also { tokenStorage?.saveToken(it.token) }

  override suspend fun loginWithTelegram(initData: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Telegram()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(TelegramAuthRequestDto(initData))
      }
    }.getOrThrow().also { tokenStorage?.saveToken(it.token) }

  override suspend fun getMe(): UserResponseDto? =
    executeGet<UserResponseDto> {
      client.get(Auth.Me())
    }.getOrThrow()

  override suspend fun linkTelegram(initData: String): UserResponseDto =
    execute<UserResponseDto> {
      client.post(Auth.LinkTelegram()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(LinkTelegramRequestDto(initData))
      }
    }.getOrThrow()
}
