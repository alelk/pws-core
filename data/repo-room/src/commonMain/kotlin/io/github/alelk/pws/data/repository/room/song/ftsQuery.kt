package io.github.alelk.pws.data.repository.room.song

/**
 * Builds an SQLite FTS5 prefix query from free-text user input.
 *
 * Each whitespace-separated token becomes a prefix term (`token*`) so that partial words match,
 * e.g. `"свят бог"` → `"свят* бог*"`. Surrounding/duplicate whitespace is collapsed and blank
 * tokens are dropped. A blank input yields an empty string (callers treat it as "no FTS query").
 */
fun buildFtsPrefixQuery(query: String): String =
  query.trim()
    .split("\\s+".toRegex())
    .filter { it.isNotBlank() }
    .joinToString(" ") { "$it*" }
