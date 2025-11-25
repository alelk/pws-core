package io.github.alelk.pws.api.contract.core.error

import io.github.alelk.pws.api.contract.core.ResourceTypeDto

fun ErrorDto.Companion.resourceNotFound(
  resourceType: ResourceTypeDto,
  resourceId: Any,
  message: String = "Resource ${resourceType.identifier} not found: $resourceId",
  details: Map<String, String>? = mapOf("resourceId" to resourceId.toString(), "resourceType" to resourceType.identifier)
) = ErrorDto(
  code = ErrorCodes.RESOURCE_NOT_FOUND,
  message = message,
  details = details
)