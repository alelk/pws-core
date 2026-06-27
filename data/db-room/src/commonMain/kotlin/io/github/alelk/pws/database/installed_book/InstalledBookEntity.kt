package io.github.alelk.pws.database.installed_book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.alelk.pws.domain.booklibrary.model.BookInstallSource
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId

@Entity(tableName = "installed_books")
data class InstalledBookEntity(
    @PrimaryKey @ColumnInfo(name = "book_id") val bookId: BookId,
    @ColumnInfo(name = "bundle_version") val bundleVersion: Version,
    @ColumnInfo(name = "installed_at") val installedAt: Long,
    @ColumnInfo(name = "source") val source: BookInstallSource,
)
