package io.github.alelk.pws.domain.booklibrary.model

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.ids.BookId

data class BookCatalogEntry(
    val bookId: BookId,
    val locales: List<Locale>,
    val name: String,
    val displayName: String,
    val bundleVersion: String,
    val downloadUrl: String,
    val fileSizeBytes: Long,
    val checksum: String,
    val songCount: Int,
)
