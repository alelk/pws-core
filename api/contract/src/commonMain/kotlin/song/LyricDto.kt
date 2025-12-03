package io.github.alelk.pws.api.contract.song

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class LyricDto(private val parts: List<LyricPartDto>) : List<LyricPartDto> by parts

