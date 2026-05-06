package io.github.alelk.pws.portable.serialization

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCCryptorStatus
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

// AES-256-CBC: Apple CommonCrypto (hardware-accelerated, FIPS-certified, side-channel safe)
// SHA-256 / HMAC-SHA-256: pure Kotlin in commonMain/Sha256.kt (deterministic, no platform API needed)

private const val AES_BLOCK_SIZE = 16

@OptIn(ExperimentalForeignApi::class)
internal actual fun aesEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
  val outputBuf = ByteArray((plaintext.size / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE)
  return memScoped {
    val moved = alloc<ULongVar>()
    key.usePinned { kp ->
      iv.usePinned { ip ->
        plaintext.usePinned { pp ->
          outputBuf.usePinned { op ->
            val status: CCCryptorStatus = CCCrypt(
              kCCEncrypt, kCCAlgorithmAES, kCCOptionPKCS7Padding,
              kp.addressOf(0), key.size.toULong(),
              ip.addressOf(0),
              pp.addressOf(0), plaintext.size.toULong(),
              op.addressOf(0), outputBuf.size.toULong(),
              moved.ptr
            )
            check(status == kCCSuccess) { "CCCrypt(encrypt) failed: $status" }
          }
        }
      }
    }
    outputBuf.copyOf(moved.value.toInt())
  }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun aesDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
  val outputBuf = ByteArray(ciphertext.size)
  return memScoped {
    val moved = alloc<ULongVar>()
    key.usePinned { kp ->
      iv.usePinned { ip ->
        ciphertext.usePinned { cp ->
          outputBuf.usePinned { op ->
            val status: CCCryptorStatus = CCCrypt(
              kCCDecrypt, kCCAlgorithmAES, kCCOptionPKCS7Padding,
              kp.addressOf(0), key.size.toULong(),
              ip.addressOf(0),
              cp.addressOf(0), ciphertext.size.toULong(),
              op.addressOf(0), outputBuf.size.toULong(),
              moved.ptr
            )
            check(status == kCCSuccess) { "CCCrypt(decrypt) failed: $status" }
          }
        }
      }
    }
    outputBuf.copyOf(moved.value.toInt())
  }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun randomBytes(size: Int): ByteArray {
  val bytes = ByteArray(size)
  bytes.usePinned { bp ->
    val status = SecRandomCopyBytes(kSecRandomDefault, size.toULong(), bp.addressOf(0))
    check(status == 0) { "SecRandomCopyBytes failed: $status" }
  }
  return bytes
}
