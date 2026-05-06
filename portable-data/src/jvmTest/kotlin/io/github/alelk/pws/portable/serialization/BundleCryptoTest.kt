package io.github.alelk.pws.portable.serialization

import io.github.alelk.pws.domain.core.Locale
import io.github.alelk.pws.domain.core.Version
import io.github.alelk.pws.domain.core.ids.BookId
import io.github.alelk.pws.portable.model.Book
import io.github.alelk.pws.portable.model.CollectionBundle
import io.github.alelk.pws.portable.model.Song
import io.github.alelk.pws.portable.model.SongNumber
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.datetime.LocalDateTime

class BundleCryptoTest : StringSpec({

  val key32 = ByteArray(32) { it.toByte() }  // deterministic test key
  val data = "Hello, encrypted world!".encodeToByteArray()

  // ── BundleCrypto unit tests ───────────────────────────────────────────────

  "encrypt → isEncrypted returns true" {
    val enc = BundleCrypto.encrypt(data, key32)
    BundleCrypto.isEncrypted(enc) shouldBe true
  }

  "plain bytes → isEncrypted returns false" {
    BundleCrypto.isEncrypted(data) shouldBe false
    BundleCrypto.isEncrypted(byteArrayOf()) shouldBe false
  }

  "encrypt → decrypt roundtrip" {
    val enc = BundleCrypto.encrypt(data, key32)
    val dec = BundleCrypto.decrypt(enc, key32)
    dec shouldBe data
  }

  "encrypt produces different ciphertext each time (random IV)" {
    val enc1 = BundleCrypto.encrypt(data, key32)
    val enc2 = BundleCrypto.encrypt(data, key32)
    enc1 shouldNotBe enc2
  }

  "decrypt with wrong key throws BundleDecryptionException" {
    val enc = BundleCrypto.encrypt(data, key32)
    val wrongKey = ByteArray(32) { 0xFF.toByte() }
    shouldThrow<BundleDecryptionException> {
      BundleCrypto.decrypt(enc, wrongKey)
    }
  }

  "decrypt corrupted ciphertext throws BundleDecryptionException" {
    val enc = BundleCrypto.encrypt(data, key32).also { it[60] = it[60].inc() }
    shouldThrow<BundleDecryptionException> {
      BundleCrypto.decrypt(enc, key32)
    }
  }

  "decrypt non-encrypted bytes throws BundleDecryptionException" {
    shouldThrow<BundleDecryptionException> {
      BundleCrypto.decrypt(data, key32)
    }
  }

  "keyFromHex roundtrip" {
    val hex = key32.joinToString("") { "%02x".format(it) }
    BundleCrypto.keyFromHex(hex) shouldBe key32
  }

  "keyFromPassphrase produces 32 bytes" {
    BundleCrypto.keyFromPassphrase("test").size shouldBe 32
  }

  "keyFromPassphrase is deterministic" {
    BundleCrypto.keyFromPassphrase("abc") shouldBe BundleCrypto.keyFromPassphrase("abc")
  }

  // ── BundleSerializer encrypted overloads ─────────────────────────────────

  val bundle = CollectionBundle(
    metadata = CollectionBundle.Metadata(
      createdAt = LocalDateTime.parse("2026-01-01T00:00:00"),
      locale = Locale.of("ru"),
      bundleId = "test-2026.1",
    ),
    books = listOf(
      Book(
        id = BookId.parse("pws-ru"),
        version = Version(1, 0),
        locales = listOf(Locale.of("ru")),
        name = "Test Book",
        displayShortName = "TB",
        displayName = "Test Book Full",
      )
    ),
    songs = listOf(
      Song(
        number = SongNumber(BookId.parse("pws-ru"), 1),
        version = Version(1, 0),
        locale = Locale.of("ru"),
        name = "Test Song",
        lyric = "Line 1\nLine 2",
      )
    ),
  )

  "CollectionBundle: encodeGzipEncrypted → decodeCollectionGzipEncrypted roundtrip" {
    val enc = BundleSerializer.encodeGzipEncrypted(bundle, key32)
    BundleCrypto.isEncrypted(enc) shouldBe true
    val decoded = BundleSerializer.decodeCollectionGzipEncrypted(enc, key32)
    decoded shouldBe bundle
  }

  "CollectionBundle: encrypted is smaller than plain text but larger than plain gzip" {
    val plain = BundleSerializer.encodeGzip(bundle)
    val enc = BundleSerializer.encodeGzipEncrypted(bundle, key32)
    // encrypted = gzip + 53-byte header + CBC padding (at most 16 bytes overhead)
    (enc.size - plain.size) shouldBe 53 + 16 // header + one CBC padding block
  }

  "decodeCollectionAuto handles plain gzip without key" {
    val plain = BundleSerializer.encodeGzip(bundle)
    val decoded = BundleSerializer.decodeCollectionAuto(plain)
    decoded shouldBe bundle
  }

  "decodeCollectionAuto handles encrypted with key" {
    val enc = BundleSerializer.encodeGzipEncrypted(bundle, key32)
    val decoded = BundleSerializer.decodeCollectionAuto(enc, key32)
    decoded shouldBe bundle
  }

  "decodeCollectionAuto encrypted without key throws" {
    val enc = BundleSerializer.encodeGzipEncrypted(bundle, key32)
    shouldThrow<IllegalArgumentException> {
      BundleSerializer.decodeCollectionAuto(enc, key = null)
    }
  }
})

