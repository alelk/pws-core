package io.github.alelk.pws.portable.model

import kotlinx.serialization.Serializable

/**
 * Catalog of available [BookBundle] files for a given release.
 *
 * Published as a plain JSON asset alongside the release (e.g. `books-catalog.json`).
 * The app downloads this first to build the "Available books" list, then fetches
 * individual [BookBundle] files on demand.
 *
 * File naming: `books-catalog.json`
 */
@Serializable
data class BookCatalog(
  val version: String,
  val books: List<BookCatalogEntry>,
)

/**
 * Metadata for a single downloadable book bundle.
 *
 * [book] carries the full [Book] descriptor — id, version, locales, display names, description.
 * [songCount] and [fileSizeBytes] are shown in the download UI before the user taps "Download".
 * [checksum] (SHA-256 hex of the `.book.yaml.gz.enc` file) lets the app skip re-downloading
 * when the file on disk is already up to date.
 */
@Serializable
data class BookCatalogEntry(
  val book: Book,
  val songCount: Int,
  val fileSizeBytes: Long,
  val checksum: String,
)
