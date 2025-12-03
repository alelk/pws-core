package io.github.alelk.pws.api.client.api

sealed interface ResourceUpdateResult<out ID> {
  data class Success<ID>(val id: ID) : ResourceUpdateResult<ID>
  data class NotFound<ID>(val id: ID) : ResourceUpdateResult<ID>
  data class ValidationError(val message: String) : ResourceUpdateResult<Nothing>
}