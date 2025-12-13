package io.github.alelk.pws.api.contract.core.error

import io.github.alelk.pws.api.contract.core.ResourceTypeDto

fun ErrorDto.Companion.resourcesAlreadyExists(
  resourceType: ResourceTypeDto,
  resourceIds: List<Any>,
  message: String = "Resources ${resourceType.identifier} already exist: $resourceIds"
) = ErrorDto(
  code = ErrorCodes.ALREADY_EXISTS,
  message = message,
  details = mapOf("resourceIds" to resourceIds.joinToString(","), "resourceType" to resourceType.identifier)
)

