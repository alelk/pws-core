package io.github.alelk.pws.api.client.http

import kotlinx.serialization.json.Json

object JsonProvider {
  val instance: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
  }
}

