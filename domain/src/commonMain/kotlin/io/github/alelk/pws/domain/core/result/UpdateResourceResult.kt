package io.github.alelk.pws.domain.core.result

sealed interface UpdateResourceResult<out R : Any> {
  // todo: nullable
  val resource: R

  data class Success<out R : Any>(override val resource: R) : UpdateResourceResult<R>

  // todo: nullable resource

  data class NotFound<out R : Any>(override val resource: R) : UpdateResourceResult<R>

  // todo: nullable resource

  data class ValidationError<out R : Any>(override val resource: R, val message: String) : UpdateResourceResult<R>

  // todo: nullable resource

  data class UnknownError<out R : Any>(
    override val resource: R,
    val exception: Throwable?,
    val message: String = exception?.message ?: exception?.let { "Unknown error: ${it::class.simpleName}" } ?: "Unknown error"
  ) : UpdateResourceResult<R>
}