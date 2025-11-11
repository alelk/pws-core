package io.github.alelk.pws.api.contract.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookSortDto {
  @SerialName("name")
  ByName,

  @SerialName("name-desc")
  ByNameDesc,

  @SerialName("priority")
  ByPriority,

  @SerialName("priority-desc")
  ByPriorityDesc
}