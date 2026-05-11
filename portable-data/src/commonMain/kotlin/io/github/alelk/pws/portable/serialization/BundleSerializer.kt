package io.github.alelk.pws.portable.serialization

import com.charleskorn.kaml.MultiLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.github.alelk.pws.portable.model.BookBundle
import io.github.alelk.pws.portable.model.CollectionBundle

/** Shared YAML instance for bundle serialization. */
internal val bundleYaml = Yaml(
  configuration = YamlConfiguration(
    multiLineStringStyle = MultiLineStringStyle.Literal,
    // Default limit is 3MB — too small for large collections (8000+ songs).
    // Set to 64MB; real-world bundles are ~10MB uncompressed YAML.
    codePointLimit = 64 * 1024 * 1024,
  )
)

/**
 * YAML + gzip serializer for [CollectionBundle] and [BookBundle].
 *
 * Platform support:
 * - JVM (Android, library manager): full support
 * - iOS / Native: full support via `platform.zlib` / `platform.CommonCrypto`
 *
 * File naming conventions (plain):
 * - `{locale}.collection.yaml.gz`     — [CollectionBundle]
 * - `{bookId}.book.yaml.gz`           — [BookBundle]
 *
 * File naming conventions (encrypted):
 * - `{locale}.collection.yaml.gz.enc` — [CollectionBundle], encrypted with [BundleCrypto]
 * - `{bookId}.book.yaml.gz.enc`       — [BookBundle], encrypted with [BundleCrypto]
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

  // ── YAML + gzip + AES-256-CBC/HMAC-SHA256 (all platforms) ───────────────
  //
  // Pipeline (encode): serialize → gzip → encrypt
  // Pipeline (decode): decrypt → ungzip → deserialize
  //
  // Gzip before encrypt: encrypted bytes are incompressible, so compress first.
  // The [key] is a 32-byte master key; see [BundleCrypto] for key helpers.

  /** Encode [bundle] as YAML, compress with gzip, then encrypt with [key]. */
  fun encodeGzipEncrypted(bundle: CollectionBundle, key: ByteArray): ByteArray =
    BundleCrypto.encrypt(encodeGzip(bundle), key)

  /**
   * Decrypt, decompress, and deserialize a [CollectionBundle].
   * @throws BundleDecryptionException if [key] is wrong or data is corrupted.
   */
  fun decodeCollectionGzipEncrypted(bytes: ByteArray, key: ByteArray): CollectionBundle =
    decodeCollectionGzip(BundleCrypto.decrypt(bytes, key))

  /** Encode [bundle] as YAML, compress with gzip, then encrypt with [key]. */
  fun encodeGzipEncrypted(bundle: BookBundle, key: ByteArray): ByteArray =
    BundleCrypto.encrypt(encodeGzip(bundle), key)

  /**
   * Decrypt, decompress, and deserialize a [BookBundle].
   * @throws BundleDecryptionException if [key] is wrong or data is corrupted.
   */
  fun decodeBookGzipEncrypted(bytes: ByteArray, key: ByteArray): BookBundle =
    decodeBookGzip(BundleCrypto.decrypt(bytes, key))

  /**
   * Decode a [CollectionBundle] from bytes that may be either plain gzip or encrypted.
   * If [key] is provided and the bytes start with the PWSB magic header, decrypts first.
   * @throws BundleDecryptionException if decryption is required but [key] is null or wrong.
   */
  fun decodeCollectionAuto(bytes: ByteArray, key: ByteArray? = null): CollectionBundle =
    if (BundleCrypto.isEncrypted(bytes)) {
      requireNotNull(key) { "Bundle is encrypted but no key was provided" }
      decodeCollectionGzipEncrypted(bytes, key)
    } else {
      decodeCollectionGzip(bytes)
    }

  /** Same as [decodeCollectionAuto] but for [BookBundle]. */
  fun decodeBookAuto(bytes: ByteArray, key: ByteArray? = null): BookBundle =
    if (BundleCrypto.isEncrypted(bytes)) {
      requireNotNull(key) { "Bundle is encrypted but no key was provided" }
      decodeBookGzipEncrypted(bytes, key)
    } else {
      decodeBookGzip(bytes)
    }
}

