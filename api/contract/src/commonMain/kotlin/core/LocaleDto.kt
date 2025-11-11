package io.github.alelk.pws.api.contract.core

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class LocaleDto(val value: String)
