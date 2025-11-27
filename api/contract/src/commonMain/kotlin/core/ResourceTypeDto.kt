package io.github.alelk.pws.api.contract.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResourceTypeDto(val identifier: String) {
  @SerialName("book")
  BOOK("book"),

  @SerialName("song")
  SONG("song"),

  @SerialName("song-number")
  SONG_NUMBER("song-number"),

  @SerialName("tag")
  TAG("tag"),

  @SerialName("song-reference")
  SONG_REFERENCE("song-reference");
}