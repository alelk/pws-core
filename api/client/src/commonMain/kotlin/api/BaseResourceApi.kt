package io.github.alelk.pws.api.client.api

import io.github.alelk.pws.api.client.error.ApiException
import io.github.alelk.pws.api.client.error.handleResponse
import io.github.alelk.pws.api.client.http.JsonProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

abstract class BaseResourceApi(protected val client: HttpClient) {
  protected val json: Json get() = JsonProvider.instance

  protected suspend inline fun <reified T : Any> executeGet(block: () -> HttpResponse): Result<T?> =
    handleResponse(json) { block() }
      .mapCatching { response ->
        if (response.status == HttpStatusCode.NotFound) null
        else execute<T> { response }.getOrThrow()
      }

  protected suspend inline fun <reified T> execute(block: () -> HttpResponse): Result<T> =
    handleResponse(json) { block() }
      .mapCatching {
        try {
          it.body<T>()
        } catch (se: Throwable) {
          throw ApiException.Serialization(se)
        }
      }
}

