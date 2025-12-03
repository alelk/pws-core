package io.github.alelk.pws.api.client.error

import io.github.alelk.pws.api.contract.core.error.ErrorDto

sealed class ApiException(message: String, cause: Throwable? = null): RuntimeException(message, cause) {
  class Network(message: String, cause: Throwable? = null): ApiException(message, cause)
  class Server(val statusCode: Int, val error: ErrorDto?): ApiException(error?.message ?: "HTTP $statusCode")
  class Serialization(cause: Throwable): ApiException("Serialization error", cause)

  class Unexpected(cause: Throwable): ApiException("Unexpected error", cause)
}

