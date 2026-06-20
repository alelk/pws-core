package io.github.alelk.pws.domain.booklibrary.model

import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId

data class InstalledBook(
    val bookId: BookId,
    val source: BookInstallSource,
    val installedAt: Long,
    val bundleVersion: Version,
)
