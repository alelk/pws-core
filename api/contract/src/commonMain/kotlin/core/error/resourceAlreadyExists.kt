package io.github.alelk.pws.api.contract.core.error

import io.github.alelk.pws.api.contract.core.ResourceTypeDto

fun ErrorDto.Companion.resourceAlreadyExists(
  resourceType: ResourceTypeDto,
  resourceId: Any,
  message: String = "Resource ${resourceType.identifier} already exists: $resourceId",
  details: Map<String, String>? = mapOf("resourceId" to resourceId.toString(), "resourceType" to resourceType.identifier)
) = ErrorDto(
  code = ErrorCodes.ALREADY_EXISTS,
  message = message,
  details = details
)