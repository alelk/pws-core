package io.github.alelk.pws.api.client.http

import io.github.alelk.pws.api.client.api.AuthApiImpl
import io.github.alelk.pws.api.client.config.NetworkConfig
import io.github.alelk.pws.api.contract.auth.RefreshRequestDto
import io.github.alelk.pws.domain.auth.storage.TokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

fun createHttpClient(
  network: NetworkConfig,
  tokenStorage: TokenStorage? = null,
  engineBuilder: (HttpClientConfig<*>.() -> Unit)? = null
): HttpClient = HttpClient {
  install(Resources)
  install(ContentNegotiation) { json(JsonProvider.instance) }
  install(HttpTimeout) {
    connectTimeoutMillis = network.connectTimeout.inWholeMilliseconds
    requestTimeoutMillis = network.requestTimeout.inWholeMilliseconds
    socketTimeoutMillis = network.socketTimeout.inWholeMilliseconds
  }
  defaultRequest {
    url.takeFrom(network.baseUrl)
  }
  if (network.enableLogging) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }
  }

  if (tokenStorage != null) {
    install(Auth) {
      bearer {
        // Attach current access token
        loadTokens {
          val tokens = tokenStorage.get()
          tokens?.let { (access, refresh) -> BearerTokens(access, refresh) }
        }
        // Attempt refresh on 401
        refreshTokens {
          val currentRefresh = tokenStorage.get()?.refreshToken
          if (currentRefresh.isNullOrBlank()) {
            null
          } else {
            val resp = try {
              val authApi = AuthApiImpl(client, tokenStorage)
              authApi.refresh(RefreshRequestDto(currentRefresh))
            } catch (_: Throwable) {
              null
            }
            resp?.let { BearerTokens(it.accessToken, it.refreshToken) }
          }
        }
        sendWithoutRequest { true }
      }
    }
  }

  engineBuilder?.invoke(this)
}

