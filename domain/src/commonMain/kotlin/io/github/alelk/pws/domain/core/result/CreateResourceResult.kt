package io.github.alelk.pws.domain.core.result

sealed interface CreateResourceResult<out R : Any> {
  data class Success<out R : Any>(val resource: R) : CreateResourceResult<R>

  sealed interface Failure<out R: Any> : CreateResourceResult<R> {
    val message: String
  }

  // todo: nullable resource + not-null existing resource

  data class AlreadyExists<out R : Any>(val resource: R, override val message: String = "Resource already exists: $resource") : Failure<R>

  // todo: nullable resource
  data class ValidationError<out R : Any>(val resource: R, override val message: String) : Failure<R>

  // todo: nullable resource
  data class UnknownError<out R : Any>(
    val resource: R,
    val exception: Throwable?,
    override val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : Failure<R>
}