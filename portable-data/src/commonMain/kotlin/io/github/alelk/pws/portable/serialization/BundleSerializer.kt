package io.github.alelk.pws.portable.serialization

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.github.alelk.pws.portable.model.BookBundle
import io.github.alelk.pws.portable.model.CollectionBundle

/** Shared YAML instance for bundle serialization. */
internal val bundleYaml = Yaml(
  configuration = YamlConfiguration(multiLineStringStyle = MultiLineStringStyle.Literal)
)

/**
 * YAML + gzip serializer for [CollectionBundle] and [BookBundle].
 *
 * Platform support:
 * - JVM (Android, library manager): full support
 * - iOS / Native: full support via `platform.zlib`
 *
 * File naming conventions:
 * - `{locale}.collection.yaml.gz` — [CollectionBundle]
 * - `{bookId}.book.yaml.gz`       — [BookBundle]
 */
object BundleSerializer {

  // ── Plain YAML (all platforms) ──────────────────────────────────────────

  fun encodeToString(bundle: CollectionBundle): String =
    bundleYaml.encodeToString(CollectionBundle.serializer(), bundle)

  fun decodeCollectionFromString(yaml: String): CollectionBundle =
    bundleYaml.decodeFromString(CollectionBundle.serializer(), yaml)

  fun encodeToString(bundle: BookBundle): String =
    bundleYaml.encodeToString(BookBundle.serializer(), bundle)

  fun decodeBookFromString(yaml: String): BookBundle =
    bundleYaml.decodeFromString(BookBundle.serializer(), yaml)

  // ── YAML + gzip (all platforms via expect/actual) ───────────────────────

  fun encodeGzip(bundle: CollectionBundle): ByteArray =
    gzip(encodeToString(bundle).encodeToByteArray())

  fun decodeCollectionGzip(bytes: ByteArray): CollectionBundle =
    decodeCollectionFromString(ungzip(bytes).decodeToString())

  fun encodeGzip(bundle: BookBundle): ByteArray =
    gzip(encodeToString(bundle).encodeToByteArray())

  fun decodeBookGzip(bytes: ByteArray): BookBundle =
    decodeBookFromString(ungzip(bytes).decodeToString())
}

