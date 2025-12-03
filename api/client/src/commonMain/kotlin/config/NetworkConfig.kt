package io.github.alelk.pws.api.client.config

import io.ktor.http.Url
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class NetworkConfig(
  val baseUrl: Url,
  val connectTimeout: Duration = 10.seconds,
  val requestTimeout: Duration = 30.seconds,
  val socketTimeout: Duration = 30.seconds,
  val enableLogging: Boolean = true
)

