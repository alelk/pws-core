package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.auth.Auth
import io.github.alelk.pws.api.contract.auth.AuthResponseDto
import io.github.alelk.pws.api.contract.auth.LinkTelegramRequestDto
import io.github.alelk.pws.api.contract.auth.LoginRequestDto
import io.github.alelk.pws.api.contract.auth.RegisterRequestDto
import io.github.alelk.pws.api.contract.auth.TelegramAuthRequestDto
import io.github.alelk.pws.api.contract.auth.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

interface AuthApi {
  suspend fun register(email: String, password: String, username: String? = null): AuthResponseDto
  suspend fun login(email: String, password: String): AuthResponseDto
  suspend fun loginWithTelegram(initData: String): AuthResponseDto
  suspend fun getMe(token: String): UserResponseDto?
  suspend fun linkTelegram(token: String, initData: String): UserResponseDto
}

internal class AuthApiImpl(client: HttpClient) : BaseResourceApi(client), AuthApi {
  override suspend fun register(email: String, password: String, username: String?): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Register()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(RegisterRequestDto(email, password, username))
      }
    }.getOrThrow()

  override suspend fun login(email: String, password: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Login()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(LoginRequestDto(email, password))
      }
    }.getOrThrow()

  override suspend fun loginWithTelegram(initData: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Telegram()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(TelegramAuthRequestDto(initData))
      }
    }.getOrThrow()

  override suspend fun getMe(token: String): UserResponseDto? =
    executeGet<UserResponseDto> {
      client.get(Auth.Me()) {
        bearerAuth(token)
      }
    }.getOrThrow()

  override suspend fun linkTelegram(token: String, initData: String): UserResponseDto =
    execute<UserResponseDto> {
      client.post(Auth.LinkTelegram()) {
        bearerAuth(token)
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(LinkTelegramRequestDto(initData))
      }
    }.getOrThrow()
}
