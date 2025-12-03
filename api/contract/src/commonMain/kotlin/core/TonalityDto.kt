package io.github.alelk.pws.api.contract.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TonalityDto(val identifier: String) {
  @SerialName("a major") A_MAJOR("a major"),
  @SerialName("a minor") A_MINOR("a minor"),
  @SerialName("a-flat major") A_FLAT_MAJOR("a-flat major"),
  @SerialName("a-flat minor") A_FLAT_MINOR("a-flat minor"),
  @SerialName("a-sharp minor") A_SHARP_MINOR("a-sharp minor"),

  @SerialName("b major") B_MAJOR("b major"),
  @SerialName("b minor") B_MINOR("b minor"),
  @SerialName("b-flat major") B_FLAT_MAJOR("b-flat major"),
  @SerialName("b-flat minor") B_FLAT_MINOR("b-flat minor"),

  @SerialName("c major") C_MAJOR("c major"),
  @SerialName("c minor") C_MINOR("c minor"),
  @SerialName("c-flat major") C_FLAT_MAJOR("c-flat major"),
  @SerialName("c-sharp major") C_SHARP_MAJOR("c-sharp major"),
  @SerialName("c-sharp minor") C_SHARP_MINOR("c-sharp minor"),

  @SerialName("d major") D_MAJOR("d major"),
  @SerialName("d minor") D_MINOR("d minor"),
  @SerialName("d-flat major") D_FLAT_MAJOR("d-flat major"),
  @SerialName("d-sharp minor") D_SHARP_MINOR("d-sharp minor"),

  @SerialName("e major") E_MAJOR("e major"),
  @SerialName("e minor") E_MINOR("e minor"),
  @SerialName("e-flat major") E_FLAT_MAJOR("e-flat major"),
  @SerialName("e-flat minor") E_FLAT_MINOR("e-flat minor"),

  @SerialName("f major") F_MAJOR("f major"),
  @SerialName("f minor") F_MINOR("f minor"),
  @SerialName("f-sharp major") F_SHARP_MAJOR("f-sharp major"),
  @SerialName("f-sharp minor") F_SHARP_MINOR("f-sharp minor"),

  @SerialName("g major") G_MAJOR("g major"),
  @SerialName("g minor") G_MINOR("g minor"),
  @SerialName("g-flat major") G_FLAT_MAJOR("g-flat major"),
  @SerialName("g-sharp minor") G_SHARP_MINOR("g-sharp minor");

  companion object {
    private val byIdentifier = entries.associateBy { it.identifier }

    fun fromIdentifier(identifier: String): TonalityDto =
      byIdentifier[identifier] ?: error("Unknown tonality: $identifier")
  }
}
