package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.portable.model.extractBook
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Golden-file compatibility tests for bundle serialization.
 *
 * ## What is tested
 * 1. **Round-trip**: encode → decode → same object (all 3 formats: YAML, gzip, encrypted gzip)
 * 2. **Stability**: decode from saved golden file → same object as current fixture
 * 3. **Encoded bytes stability**: encode from fixture → same bytes as golden file
 *    (ensures YAML field ordering and gzip output don't drift between library versions)
 *
 * ## Golden files (in jvmTest/resources/golden/)
 * | File | Format |
 * |---|---|
 * | `collection.v1.yaml`        | plain YAML, human-readable, version-controlled |
 * | `collection.v1.yaml.gz`     | gzip-compressed YAML |
 * | `collection.v1.yaml.gz.enc` | AES-256-CBC + HMAC-SHA256 encrypted gzip |
 * | `book.v1.yaml`              | BookBundle plain YAML |
 * | `book.v1.yaml.gz`           | BookBundle gzip |
 * | `book.v1.yaml.gz.enc`       | BookBundle encrypted gzip |
 *
 * ## Regenerating golden files
 * Set the environment variable `UPDATE_GOLDEN=true` before running tests:
 * ```shell
 * UPDATE_GOLDEN=true ./gradlew :portable-data:jvmTest --tests "*.BundleGoldenFileTest"
 * ```
 * Commit the updated files together with the model changes.
 * NEVER update golden files without a corresponding model/serializer change explanation.
 */
class BundleGoldenFileTest : StringSpec({

  val updateGolden = System.getenv("UPDATE_GOLDEN") == "true"
  val goldenDir = goldenDir()

  // ── Helpers ─────────────────────────────────────────────────────────────

  fun loadGolden(name: String): ByteArray? {
    if (updateGolden) return null  // skip load when regenerating
    return checkNotNull(BundleGoldenFileTest::class.java.getResourceAsStream("/golden/$name")) {
      "Golden file not found: /golden/$name — run with UPDATE_GOLDEN=true to generate"
    }.readBytes()
  }

  fun saveGolden(name: String, bytes: ByteArray) {
    goldenDir.resolve(name).also { it.parentFile.mkdirs() }.writeBytes(bytes)
    println("[golden] written: $name (${bytes.size} bytes)")
  }

  /** Write if UPDATE_GOLDEN; returns null so callers skip assertions on first generation. */
  fun goldenBytes(name: String, current: ByteArray): ByteArray? {
    if (updateGolden) { saveGolden(name, current); return null }
    return loadGolden(name)
  }

  /** Convenience: only run [block] when not in update mode and golden file is available. */
  fun withGolden(name: String, current: ByteArray, block: (ByteArray) -> Unit) {
    val bytes = goldenBytes(name, current) ?: return   // null only when updateGolden=true
    block(bytes)
  }

  // ── CollectionBundle — plain YAML ────────────────────────────────────────

  "CollectionBundle YAML: encode → decode roundtrip" {
    val yaml = BundleSerializer.encodeToString(BundleFixtures.COLLECTION_BUNDLE)
    val decoded = BundleSerializer.decodeCollectionFromString(yaml)
    decoded shouldBe BundleFixtures.COLLECTION_BUNDLE
  }

  "CollectionBundle YAML: golden file decode → expected fixture" {
    withGolden("collection.v1.yaml", BundleSerializer.encodeToString(BundleFixtures.COLLECTION_BUNDLE).encodeToByteArray()) { bytes ->
      BundleSerializer.decodeCollectionFromString(bytes.decodeToString()) shouldBe BundleFixtures.COLLECTION_BUNDLE
    }
  }

  "CollectionBundle YAML: encode matches golden file bytes exactly" {
    val current = BundleSerializer.encodeToString(BundleFixtures.COLLECTION_BUNDLE).encodeToByteArray()
    withGolden("collection.v1.yaml", current) { golden ->
      current shouldBe golden
    }
  }

  // ── CollectionBundle — gzip ──────────────────────────────────────────────

  "CollectionBundle GZIP: encode → decode roundtrip" {
    val gz = BundleSerializer.encodeGzip(BundleFixtures.COLLECTION_BUNDLE)
    BundleSerializer.decodeCollectionGzip(gz) shouldBe BundleFixtures.COLLECTION_BUNDLE
  }

  "CollectionBundle GZIP: golden file decode → expected fixture" {
    withGolden("collection.v1.yaml.gz", BundleSerializer.encodeGzip(BundleFixtures.COLLECTION_BUNDLE)) { gz ->
      BundleSerializer.decodeCollectionGzip(gz) shouldBe BundleFixtures.COLLECTION_BUNDLE
    }
  }

  "CollectionBundle GZIP: gzip is smaller than plain YAML" {
    val yaml = BundleSerializer.encodeToString(BundleFixtures.COLLECTION_BUNDLE).encodeToByteArray()
    val gz = BundleSerializer.encodeGzip(BundleFixtures.COLLECTION_BUNDLE)
    gz.size shouldBeLessThan yaml.size
  }

  // ── CollectionBundle — encrypted gzip ────────────────────────────────────

  "CollectionBundle ENC: encode → decode roundtrip" {
    val enc = BundleSerializer.encodeGzipEncrypted(BundleFixtures.COLLECTION_BUNDLE, BundleFixtures.ENCRYPTION_KEY)
    BundleCrypto.isEncrypted(enc) shouldBe true
    BundleSerializer.decodeCollectionGzipEncrypted(enc, BundleFixtures.ENCRYPTION_KEY) shouldBe BundleFixtures.COLLECTION_BUNDLE
  }

  "CollectionBundle ENC: golden file decode → expected fixture" {
    withGolden("collection.v1.yaml.gz.enc", BundleSerializer.encodeGzipEncrypted(BundleFixtures.COLLECTION_BUNDLE, BundleFixtures.ENCRYPTION_KEY)) { enc ->
      BundleSerializer.decodeCollectionGzipEncrypted(enc, BundleFixtures.ENCRYPTION_KEY) shouldBe BundleFixtures.COLLECTION_BUNDLE
    }
  }

  "CollectionBundle ENC: auto-decode with correct key" {
    withGolden("collection.v1.yaml.gz.enc", BundleSerializer.encodeGzipEncrypted(BundleFixtures.COLLECTION_BUNDLE, BundleFixtures.ENCRYPTION_KEY)) { enc ->
      BundleSerializer.decodeCollectionAuto(enc, BundleFixtures.ENCRYPTION_KEY) shouldBe BundleFixtures.COLLECTION_BUNDLE
    }
  }

  "CollectionBundle ENC: auto-decode plain gzip without key" {
    withGolden("collection.v1.yaml.gz", BundleSerializer.encodeGzip(BundleFixtures.COLLECTION_BUNDLE)) { gz ->
      BundleSerializer.decodeCollectionAuto(gz) shouldBe BundleFixtures.COLLECTION_BUNDLE
    }
  }

  // ── BookBundle — plain YAML ───────────────────────────────────────────────

  "BookBundle YAML: encode → decode roundtrip" {
    val yaml = BundleSerializer.encodeToString(BundleFixtures.BOOK_BUNDLE)
    BundleSerializer.decodeBookFromString(yaml) shouldBe BundleFixtures.BOOK_BUNDLE
  }

  "BookBundle YAML: golden file decode → expected fixture" {
    withGolden("book.v1.yaml", BundleSerializer.encodeToString(BundleFixtures.BOOK_BUNDLE).encodeToByteArray()) { bytes ->
      BundleSerializer.decodeBookFromString(bytes.decodeToString()) shouldBe BundleFixtures.BOOK_BUNDLE
    }
  }

  "BookBundle YAML: encode matches golden file bytes exactly" {
    val current = BundleSerializer.encodeToString(BundleFixtures.BOOK_BUNDLE).encodeToByteArray()
    withGolden("book.v1.yaml", current) { golden ->
      current shouldBe golden
    }
  }

  // ── BookBundle — gzip ─────────────────────────────────────────────────────

  "BookBundle GZIP: encode → decode roundtrip" {
    val gz = BundleSerializer.encodeGzip(BundleFixtures.BOOK_BUNDLE)
    BundleSerializer.decodeBookGzip(gz) shouldBe BundleFixtures.BOOK_BUNDLE
  }

  "BookBundle GZIP: golden file decode → expected fixture" {
    withGolden("book.v1.yaml.gz", BundleSerializer.encodeGzip(BundleFixtures.BOOK_BUNDLE)) { gz ->
      BundleSerializer.decodeBookGzip(gz) shouldBe BundleFixtures.BOOK_BUNDLE
    }
  }

  // ── BookBundle — encrypted gzip ───────────────────────────────────────────

  "BookBundle ENC: encode → decode roundtrip" {
    val enc = BundleSerializer.encodeGzipEncrypted(BundleFixtures.BOOK_BUNDLE, BundleFixtures.ENCRYPTION_KEY)
    BundleSerializer.decodeBookGzipEncrypted(enc, BundleFixtures.ENCRYPTION_KEY) shouldBe BundleFixtures.BOOK_BUNDLE
  }

  "BookBundle ENC: golden file decode → expected fixture" {
    withGolden("book.v1.yaml.gz.enc", BundleSerializer.encodeGzipEncrypted(BundleFixtures.BOOK_BUNDLE, BundleFixtures.ENCRYPTION_KEY)) { enc ->
      BundleSerializer.decodeBookGzipEncrypted(enc, BundleFixtures.ENCRYPTION_KEY) shouldBe BundleFixtures.BOOK_BUNDLE
    }
  }

  // ── extractBook compatibility ─────────────────────────────────────────────

  "extractBook from golden collection equals golden book bundle" {
    withGolden("collection.v1.yaml.gz", BundleSerializer.encodeGzip(BundleFixtures.COLLECTION_BUNDLE)) { gz ->
      val collection = BundleSerializer.decodeCollectionGzip(gz)
      collection.extractBook(BookId.parse("pws-ru")) shouldBe BundleFixtures.BOOK_BUNDLE
    }
  }
})










