package io.github.alelk.pws.portable.serialization

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Known-Answer Tests for the pure-Kotlin SHA-256 and HMAC-SHA-256 implementations.
 * Runs on ALL platforms (JVM, iOS, ...).
 *
 * SHA-256 vectors: NIST FIPS 180-4 / NIST CAVP
 * HMAC vectors: RFC 4231
 *
 * All expected values verified with Python's hashlib / hmac modules.
 */
class Sha256KatTest : StringSpec({

  fun hex(bytes: ByteArray) = bytes.joinToString("") { it.toInt().and(0xff).toString(16).padStart(2, '0') }
  fun fromHex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

  // ── SHA-256 Known-Answer Tests ─────────────────────────────────────────

  "SHA-256: empty input" {
    hex(sha256(byteArrayOf())) shouldBe
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }

  "SHA-256: 'abc' (FIPS 180-4 Example 1)" {
    hex(sha256("abc".encodeToByteArray())) shouldBe
      "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
  }

  "SHA-256: 448-bit message (FIPS 180-4 Example 2)" {
    hex(sha256("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray())) shouldBe
      "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1"
  }

  "SHA-256: 'a' × 1 000 000 (FIPS 180-4 Example 3)" {
    hex(sha256(ByteArray(1_000_000) { 'a'.code.toByte() })) shouldBe
      "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0"
  }

  "SHA-256: single byte 0xbd (NIST CAVP)" {
    hex(sha256(fromHex("bd"))) shouldBe
      "68325720aabd7c82f30f554b313d0570c95accbb7dc4b5aae11204c08ffe732b"
  }

  "SHA-256: 4 bytes 0xc98c8e55 (NIST CAVP)" {
    hex(sha256(fromHex("c98c8e55"))) shouldBe
      "7abc22c0ae5af26ce93dbb94433a0e0b2e119d014f8e7f65bd56c61ccccd9504"
  }

  // ── HMAC-SHA-256 Known-Answer Tests (RFC 4231) ─────────────────────────

  "HMAC-SHA-256: RFC 4231 TC-1 (20-byte 0x0b key, 'Hi There')" {
    hex(hmacSha256("Hi There".encodeToByteArray(), fromHex("0b".repeat(20)))) shouldBe
      "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7"
  }

  "HMAC-SHA-256: RFC 4231 TC-2 (key = 'Jefe')" {
    hex(hmacSha256("what do ya want for nothing?".encodeToByteArray(), "Jefe".encodeToByteArray())) shouldBe
      "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843"
  }

  "HMAC-SHA-256: RFC 4231 TC-3 (20-byte 0xaa key, 50-byte 0xdd data)" {
    hex(hmacSha256(ByteArray(50) { 0xdd.toByte() }, ByteArray(20) { 0xaa.toByte() })) shouldBe
      "773ea91e36800e46854db8ebd09181a72959098b3ef8c122d9635514ced565fe"
  }

  "HMAC-SHA-256: RFC 4231 TC-4 (25-byte counter key, 50-byte 0xcd data)" {
    val key = fromHex("0102030405060708090a0b0c0d0e0f10111213141516171819")
    hex(hmacSha256(ByteArray(50) { 0xcd.toByte() }, key)) shouldBe
      "82558a389a443c0ea4cc819899f2083a85f0faa3e578f8077a2e3ff46729665b"
  }

  "HMAC-SHA-256: RFC 4231 TC-6 (key longer than block size)" {
    val key = ByteArray(131) { 0xaa.toByte() }
    val data = "Test Using Larger Than Block-Size Key - Hash Key First".encodeToByteArray()
    hex(hmacSha256(data, key)) shouldBe
      "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54"
  }

  "HMAC-SHA-256: RFC 4231 TC-7 (key and data both longer than block size)" {
    val key = ByteArray(131) { 0xaa.toByte() }
    val data = ("This is a test using a larger than block-size key and a larger than " +
      "block-size data. The key needs to be hashed before being used by the HMAC algorithm.").encodeToByteArray()
    hex(hmacSha256(data, key)) shouldBe
      "9b09ffa71b942fcb27635fbcd5b0e944bfdc63644f0713938a7f51535c3a35e2"
  }
})

