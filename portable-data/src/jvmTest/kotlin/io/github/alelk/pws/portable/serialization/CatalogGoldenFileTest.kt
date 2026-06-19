package io.github.alelk.pws.portable.serialization

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Golden-file compatibility tests for [io.github.alelk.pws.portable.model.BookCatalog] JSON serialization.
 *
 * ## What is tested
 * 1. **Round-trip**: encode → decode → same object
 * 2. **Stability**: decode from saved golden file → same object as current fixture
 * 3. **Encoded bytes stability**: encode from fixture → same bytes as golden file
 *    (ensures JSON field ordering doesn't drift between library versions)
 *
 * ## Golden file (in jvmTest/resources/golden/)
 * | File | Format |
 * |---|---|
 * | `catalog.v1.json` | plain JSON, human-readable, version-controlled |
 *
 * ## Regenerating golden files
 * Set the environment variable `UPDATE_GOLDEN=true` before running tests:
 * ```shell
 * UPDATE_GOLDEN=true ./gradlew :portable-data:jvmTest --tests "*.CatalogGoldenFileTest"
 * ```
 * Commit the updated file together with the model change.
 * NEVER update golden files without a corresponding model/serializer change explanation.
 */
class CatalogGoldenFileTest : StringSpec({

  val updateGolden = System.getenv("UPDATE_GOLDEN") == "true"
  val goldenDir = goldenDir()

  fun loadGolden(name: String): String? {
    if (updateGolden) return null
    return checkNotNull(CatalogGoldenFileTest::class.java.getResourceAsStream("/golden/$name")) {
      "Golden file not found: /golden/$name — run with UPDATE_GOLDEN=true to generate"
    }.readBytes().decodeToString()
  }

  fun saveGolden(name: String, content: String) {
    goldenDir.resolve(name).also { it.parentFile.mkdirs() }.writeText(content)
    println("[golden] written: $name (${content.length} chars)")
  }

  fun withGolden(name: String, current: String, block: (String) -> Unit) {
    if (updateGolden) { saveGolden(name, current); return }
    val golden = loadGolden(name) ?: return
    block(golden)
  }

  // ── BookCatalog — JSON ────────────────────────────────────────────────────

  "BookCatalog JSON: encode → decode roundtrip" {
    val json = CatalogSerializer.encode(BundleFixtures.BOOK_CATALOG)
    CatalogSerializer.decode(json) shouldBe BundleFixtures.BOOK_CATALOG
  }

  "BookCatalog JSON: golden file decode → expected fixture" {
    val current = CatalogSerializer.encode(BundleFixtures.BOOK_CATALOG)
    withGolden("catalog.v1.json", current) { golden ->
      CatalogSerializer.decode(golden) shouldBe BundleFixtures.BOOK_CATALOG
    }
  }

  "BookCatalog JSON: encode matches golden file exactly" {
    val current = CatalogSerializer.encode(BundleFixtures.BOOK_CATALOG)
    withGolden("catalog.v1.json", current) { golden ->
      current shouldBe golden
    }
  }
})
