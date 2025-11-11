package io.github.alelk.pws.api.client.error

import io.github.alelk.pws.api.contract.core.error.ErrorDto
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

suspend inline fun handleResponse(json: Json, block: suspend () -> HttpResponse): Result<HttpResponse> {
  val response = try {
    block()
  } catch (t: Throwable) {
    throw ApiException.Network(t.message ?: "network error", t)
  }
  return if (response.status.isSuccess()) {
    Result.success(response)
  } else {
    val serverError =
      try {
        json.decodeFromString<ErrorDto>(response.bodyAsText())
      } catch (_: SerializationException) {
        null
      }
    Result.failure(ApiException.Server(response.status.value, serverError))
  }
}

