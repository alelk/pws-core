package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.contract.auth.*
import io.github.alelk.pws.api.mapping.auth.authTokens
import io.github.alelk.pws.domain.auth.model.AuthTokens
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

  suspend fun refresh(request: RefreshRequestDto): RefreshResponseDto
  suspend fun logout()
}

internal class AuthApiImpl(
  client: HttpClient,
  val tokenStorage: TokenStorage
) : BaseResourceApi(client), AuthApi {

  override suspend fun register(email: String, password: String, username: String?): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Register()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(RegisterRequestDto(email, password, username))
      }
    }.getOrThrow().also { tokenStorage.save(it.authTokens) }

  override suspend fun login(email: String, password: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Login()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(LoginRequestDto(email, password))
      }
    }.getOrThrow().also { tokenStorage.save(it.authTokens) }

  override suspend fun loginWithTelegram(initData: String): AuthResponseDto =
    execute<AuthResponseDto> {
      client.post(Auth.Telegram()) {
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(TelegramAuthRequestDto(initData))
      }
    }.getOrThrow().also { tokenStorage.save(it.authTokens) }

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

  override suspend fun refresh(request: RefreshRequestDto): RefreshResponseDto =
    execute<RefreshResponseDto> {
      client.post(Auth.Logout())
    }.getOrThrow().also { tokenStorage.save(AuthTokens(refreshToken = it.refreshToken, accessToken = it.accessToken)) }

  override suspend fun logout() {
    execute<Unit> {
      client.post(Auth.Logout())
    }
    tokenStorage.clear()
  }
}
