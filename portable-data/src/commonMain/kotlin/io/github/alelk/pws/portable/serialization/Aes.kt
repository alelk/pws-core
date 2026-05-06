package io.github.alelk.pws.portable.serialization

/**
 * Platform-specific AES-256-CBC encryption/decryption and random byte generation.
 *
 * SHA-256 and HMAC-SHA-256 are in pure Kotlin ([Sha256.kt]) — no platform code needed.
 *
 * - JVM (Android + library-manager): `javax.crypto` — built-in, hardware-accelerated
 * - iOS / Native: `platform.CommonCrypto.CCCrypt` + `platform.Security.SecRandomCopyBytes`
 *   (requires `-framework CommonCrypto` linker flag — configured in build.gradle.kts)
 */

/** Encrypt [plaintext] with AES-256-CBC (PKCS7 padding). [key] = 32 bytes, [iv] = 16 bytes. */
internal expect fun aesEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

/** Decrypt [ciphertext] with AES-256-CBC (PKCS7 padding). [key] = 32 bytes, [iv] = 16 bytes. */
internal expect fun aesDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

/** Generate [size] cryptographically random bytes. */
internal expect fun randomBytes(size: Int): ByteArray
