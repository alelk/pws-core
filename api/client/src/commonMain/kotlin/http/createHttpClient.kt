package io.github.alelk.pws.api.client.http

import io.github.alelk.pws.api.client.config.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json

fun createHttpClient(
  network: NetworkConfig,
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
  engineBuilder?.invoke(this)
}

