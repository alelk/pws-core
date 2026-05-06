package io.github.alelk.pws.portable.serialization

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Boundary-length SHA-256 tests — JVM only.
 * Cross-checks our pure-Kotlin SHA-256 against java.security.MessageDigest
 * for edge-case input sizes around padding boundaries (55, 56, 64, 127, 128 bytes).
 */
class Sha256BoundaryTest : StringSpec({

  fun sha256jvm(data: ByteArray): String =
    java.security.MessageDigest.getInstance("SHA-256").digest(data)
      .joinToString("") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

  fun hex(bytes: ByteArray) =
    bytes.joinToString("") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

  "SHA-256: 55 bytes (single-block padding: data + 0x80 + zeros + length = 64 bytes)" {
    hex(sha256(ByteArray(55) { it.toByte() })) shouldBe sha256jvm(ByteArray(55) { it.toByte() })
  }

  "SHA-256: 56 bytes (two-block padding: length overflows to second block)" {
    hex(sha256(ByteArray(56) { it.toByte() })) shouldBe sha256jvm(ByteArray(56) { it.toByte() })
  }

  "SHA-256: 64 bytes (exactly one block, padding in second block)" {
    hex(sha256(ByteArray(64) { it.toByte() })) shouldBe sha256jvm(ByteArray(64) { it.toByte() })
  }

  "SHA-256: 127 bytes (two full blocks minus one byte)" {
    hex(sha256(ByteArray(127) { it.toByte() })) shouldBe sha256jvm(ByteArray(127) { it.toByte() })
  }

  "SHA-256: 128 bytes (exactly two blocks)" {
    hex(sha256(ByteArray(128) { it.toByte() })) shouldBe sha256jvm(ByteArray(128) { it.toByte() })
  }
})

