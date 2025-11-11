package io.github.alelk.pws.api.contract.core.error

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(val code: String, val message: String)