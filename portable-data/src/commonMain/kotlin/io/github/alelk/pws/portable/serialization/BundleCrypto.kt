package io.github.alelk.pws.portable.serialization

/**
 * Authenticated encryption for bundle files (CollectionBundle / BookBundle).
 *
 * ## Algorithm
 * AES-256-CBC (PKCS7 padding) + HMAC-SHA256 — Encrypt-then-MAC construction.
 *
 * Two sub-keys are derived from the master key via SHA-256 so the encryption key
 * and the MAC key are always independent:
 * ```
 * enc_key = SHA-256("pws-enc" || masterKey)   // 32 bytes → AES-256
 * mac_key = SHA-256("pws-mac" || masterKey)   // 32 bytes → HMAC-SHA256
 * ```
 *
 * ## Wire format
 * ```
 * Offset  Size   Field
 * 0       4      Magic "PWSB"
 * 4       1      Version (0x01)
 * 5       32     HMAC-SHA256(mac_key, IV || ciphertext)
 * 37      16     IV  (random, AES block size)
 * 53      N      AES-256-CBC(enc_key, IV, plaintext)  — PKCS7 padded
 * ```
 * Total overhead: 53 bytes.
 *
 * ## Platform implementations
 * - JVM (Android + library-manager): `javax.crypto` — built-in, no extra deps
 * - iOS / Native: `platform.CommonCrypto` (`CCCrypt` + `CCHmac` + `CC_SHA256`) +
 *   `platform.Security` (`SecRandomCopyBytes`) — standard Apple headers, no SPI required
 *
 * ## Typical usage
 * ```kotlin
 * val key = BundleCrypto.keyFromPassphrase("my-secret")  // or provide raw 32-byte key
 * val encrypted = BundleCrypto.encrypt(BundleSerializer.encodeGzip(bundle), key)
 * // later:
 * val plain = BundleCrypto.decrypt(encrypted, key)
 * val bundle = BundleSerializer.decodeCollectionGzip(plain)
 * ```
 */
object BundleCrypto {

  private val MAGIC = byteArrayOf('P'.code.toByte(), 'W'.code.toByte(), 'S'.code.toByte(), 'B'.code.toByte())
  private const val VERSION: Byte = 1
  private const val IV_SIZE = 16      // AES block size
  private const val HMAC_SIZE = 32    // SHA-256 output
  private const val HEADER_SIZE = 4 + 1 + HMAC_SIZE + IV_SIZE  // = 53 bytes

  // Offsets inside the header
  private const val HMAC_OFFSET = 5
  private const val IV_OFFSET = HMAC_OFFSET + HMAC_SIZE          // = 37
  private const val PAYLOAD_OFFSET = IV_OFFSET + IV_SIZE         // = 53

  /**
   * Encrypt [data] (typically the result of [BundleSerializer.encodeGzip]) with [masterKey].
   *
   * @param masterKey 32-byte raw key. Use [keyFromHex] or [keyFromPassphrase] to derive one.
   * @return Encrypted bytes in the PWSB wire format.
   */
  fun encrypt(data: ByteArray, masterKey: ByteArray): ByteArray {
    requireKeySize(masterKey)
    val encKey = sha256("pws-enc".encodeToByteArray() + masterKey)
    val macKey = sha256("pws-mac".encodeToByteArray() + masterKey)
    val iv = randomBytes(IV_SIZE)
    val ciphertext = aesEncrypt(data, encKey, iv)
    val hmac = hmacSha256(iv + ciphertext, macKey)
    return MAGIC + byteArrayOf(VERSION) + hmac + iv + ciphertext
  }

  /**
   * Decrypt [data] produced by [encrypt].
   *
   * @param masterKey 32-byte raw key.
   * @throws BundleDecryptionException if the magic header is missing, the HMAC verification fails,
   *   or the ciphertext cannot be decrypted (wrong key or corrupted data).
   */
  fun decrypt(data: ByteArray, masterKey: ByteArray): ByteArray {
    requireKeySize(masterKey)
    if (!isEncrypted(data)) throw BundleDecryptionException("Not an encrypted bundle (missing PWSB magic header)")
    val version = data[4]
    if (version != VERSION) throw BundleDecryptionException("Unsupported bundle encryption version: $version")

    val encKey = sha256("pws-enc".encodeToByteArray() + masterKey)
    val macKey = sha256("pws-mac".encodeToByteArray() + masterKey)

    val storedHmac = data.copyOfRange(HMAC_OFFSET, HMAC_OFFSET + HMAC_SIZE)
    val iv = data.copyOfRange(IV_OFFSET, IV_OFFSET + IV_SIZE)
    val ciphertext = data.copyOfRange(PAYLOAD_OFFSET, data.size)

    val expectedHmac = hmacSha256(iv + ciphertext, macKey)
    if (!constantTimeEquals(storedHmac, expectedHmac))
      throw BundleDecryptionException("HMAC verification failed: wrong key or corrupted data")

    return try {
      aesDecrypt(ciphertext, encKey, iv)
    } catch (e: Exception) {
      throw BundleDecryptionException("AES decryption failed", e)
    }
  }

  /**
   * Returns `true` if [data] starts with the PWSB magic header (i.e. was produced by [encrypt]).
   * Use this to decide whether to call [decrypt] or pass data directly to [BundleSerializer].
   */
  fun isEncrypted(data: ByteArray): Boolean =
    data.size > HEADER_SIZE && data.sliceArray(0 until 4).contentEquals(MAGIC)

  // ── Key helpers ────────────────────────────────────────────────────────────

  /**
   * Decode a 64-character hex string to a 32-byte key.
   * Useful for reading a key from a CLI argument or environment variable.
   */
  fun keyFromHex(hex: String): ByteArray {
    require(hex.length == 64) { "Hex key must be 64 characters (32 bytes), got ${hex.length}" }
    return ByteArray(32) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
  }

  /**
   * Derive a 32-byte key from an arbitrary passphrase via SHA-256.
   * For production use, prefer a proper KDF (PBKDF2 / Argon2) with a stored salt.
   */
  fun keyFromPassphrase(passphrase: String): ByteArray =
    sha256(passphrase.encodeToByteArray())

  // ── Internal helpers ───────────────────────────────────────────────────────

  private fun requireKeySize(key: ByteArray) =
    require(key.size == 32) { "masterKey must be exactly 32 bytes (AES-256), got ${key.size}" }

  /** Constant-time byte-array comparison to prevent timing attacks on HMAC verification. */
  private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var result = 0
    for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
    return result == 0
  }
}

/** Thrown when bundle decryption fails (wrong key, corrupted data, or unsupported version). */
class BundleDecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

