package io.github.alelk.pws.api.contract.core.error

import io.github.alelk.pws.api.contract.core.ResourceTypeDto

fun ErrorDto.Companion.resourcesNotFound(
  resourceType: ResourceTypeDto,
  resourceIds: List<Any>,
  message: String = "Resources ${resourceType.identifier} not found: ${resourceIds.joinToString(", ")}",
  details: Map<String, String>? = mapOf("resourceType" to resourceType.identifier)
) = ErrorDto(
  code = ErrorCodes.RESOURCES_NOT_FOUND,
  message = message,
  details = mapOf("resourceType" to resourceType.identifier, "resourceIds" to resourceIds.joinToString(",")) + (details ?: emptyMap())
)