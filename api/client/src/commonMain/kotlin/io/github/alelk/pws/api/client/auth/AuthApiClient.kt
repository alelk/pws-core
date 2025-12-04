package io.github.alelk.pws.api.client.auth

import io.github.alelk.pws.api.contract.auth.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*

class AuthApiClient(private val httpClient: HttpClient) {

  suspend fun register(email: String, password: String, username: String? = null): Result<AuthResponseDto> =
    runCatching {
      httpClient.post(Auth.Register()) {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequestDto(email, password, username))
      }.body()
    }

  suspend fun login(email: String, password: String): Result<AuthResponseDto> =
    runCatching {
      httpClient.post(Auth.Login()) {
        contentType(ContentType.Application.Json)
        setBody(LoginRequestDto(email, password))
      }.body()
    }

  suspend fun loginWithTelegram(initData: String): Result<AuthResponseDto> =
    runCatching {
      httpClient.post(Auth.Telegram()) {
        contentType(ContentType.Application.Json)
        setBody(TelegramAuthRequestDto(initData))
      }.body()
    }

  suspend fun getMe(token: String): Result<UserResponseDto> =
    runCatching {
      httpClient.get(Auth.Me()) {
        bearerAuth(token)
      }.body()
    }

  suspend fun linkTelegram(token: String, initData: String): Result<UserResponseDto> =
    runCatching {
      httpClient.post(Auth.LinkTelegram()) {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(LinkTelegramRequestDto(initData))
      }.body()
    }
}

