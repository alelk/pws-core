package io.github.alelk.pws.api.client.api

sealed interface ResourceCreateResult<out ID> {
  data class Success<ID>(val id: ID) : ResourceCreateResult<ID>
  data class AlreadyExists<ID>(val id: ID) : ResourceCreateResult<ID>
  data class ValidationError(val message: String) : ResourceCreateResult<Nothing>
}