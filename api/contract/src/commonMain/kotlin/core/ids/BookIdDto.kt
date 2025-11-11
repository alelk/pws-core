package io.github.alelk.pws.api.contract.core.ids

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class BookIdDto(val value: String)