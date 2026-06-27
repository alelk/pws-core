package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.portable.model.BookCatalog
import kotlinx.serialization.json.Json

private val catalogJson = Json {
  prettyPrint = true
  ignoreUnknownKeys = true  // forward compat: newer catalog versions may add fields
}

object CatalogSerializer {
  fun encode(catalog: BookCatalog): String =
    catalogJson.encodeToString(BookCatalog.serializer(), catalog)

  fun decode(json: String): BookCatalog =
    catalogJson.decodeFromString(BookCatalog.serializer(), json)
}
