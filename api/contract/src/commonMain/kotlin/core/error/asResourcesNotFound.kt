package io.github.alelk.pws.api.contract.core.error

import io.github.alelk.pws.api.contract.core.ResourceTypeDto

data class ResourcesNotFoundErrorDto(
  val resourceType: ResourceTypeDto,
  val message: String,
  val resourceIds: List<String> = emptyList(),
  val details: Map<String, String>? = emptyMap()
)

fun ErrorDto.asResourcesNotFound() =
  ResourcesNotFoundErrorDto(
    resourceType = ResourceTypeDto.fromIdentifier(requireNotNull(details?.get("resourceType")) { "resourceType field is required" }),
    message = message,
    resourceIds = requireNotNull(details["resourceIds"]) { "resourceIds field is required" }.split(","),
    details = details - "resourceType" - "resourceIds"
  )