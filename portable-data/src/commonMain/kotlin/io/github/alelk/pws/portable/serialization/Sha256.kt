package io.github.alelk.pws.portable.serialization

// FIPS 180-4 SHA-256 in pure Kotlin — no platform APIs, works on all targets.

private val SHA256_K = intArrayOf(
  0x428a2f98, 0x71374491, 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
  0x3956c25b, 0x59f111f1, 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
  0xd807aa98.toInt(), 0x12835b01, 0x243185be, 0x550c7dc3,
  0x72be5d74, 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
  0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6, 0x240ca1cc,
  0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
  0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
  0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351, 0x14292967,
  0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
  0x650a7354, 0x766a0abb, 0x81c2c92e.toInt(), 0x92722c85.toInt(),
  0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
  0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070,
  0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
  0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
  0x748f82ee, 0x78a5636f, 0x84c87814.toInt(), 0x8cc70208.toInt(),
  0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt(),
)

// Rotate right without using stdlib extension (guaranteed available on all KMP targets)
private fun Int.rotR(n: Int): Int = (this ushr n) or (this shl (32 - n))

/** SHA-256 digest — pure Kotlin, all platforms. Returns 32 bytes. */
internal fun sha256(data: ByteArray): ByteArray {
  val bitLen = data.size.toLong() * 8
  // Padded length: next multiple of 64 after (data + 0x80 + 8-byte length)
  val totalLen = ((data.size + 9 + 63) / 64) * 64
  val msg = ByteArray(totalLen)
  data.copyInto(msg)
  msg[data.size] = 0x80.toByte()
  for (i in 0..7) msg[totalLen - 8 + i] = (bitLen ushr ((7 - i) * 8)).toByte()

  val h = intArrayOf(
    0x6a09e667, 0xbb67ae85.toInt(), 0x3c6ef372, 0xa54ff53a.toInt(),
    0x510e527f, 0x9b05688c.toInt(), 0x1f83d9ab, 0x5be0cd19,
  )
  val w = IntArray(64)

  for (chunk in 0 until totalLen / 64) {
    val off = chunk * 64
    for (i in 0..15) {
      w[i] = ((msg[off + i * 4].toInt() and 0xFF) shl 24) or
        ((msg[off + i * 4 + 1].toInt() and 0xFF) shl 16) or
        ((msg[off + i * 4 + 2].toInt() and 0xFF) shl 8) or
        (msg[off + i * 4 + 3].toInt() and 0xFF)
    }
    for (i in 16..63) {
      val s0 = w[i - 15].rotR(7) xor w[i - 15].rotR(18) xor (w[i - 15] ushr 3)
      val s1 = w[i - 2].rotR(17) xor w[i - 2].rotR(19) xor (w[i - 2] ushr 10)
      w[i] = w[i - 16] + s0 + w[i - 7] + s1
    }
    var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
    var e = h[4]; var f = h[5]; var g = h[6]; var hv = h[7]
    for (i in 0..63) {
      val S1 = e.rotR(6) xor e.rotR(11) xor e.rotR(25)
      val ch = (e and f) xor (e.inv() and g)
      val temp1 = hv + S1 + ch + SHA256_K[i] + w[i]
      val S0 = a.rotR(2) xor a.rotR(13) xor a.rotR(22)
      val maj = (a and b) xor (a and c) xor (b and c)
      val temp2 = S0 + maj
      hv = g; g = f; f = e; e = d + temp1
      d = c; c = b; b = a; a = temp1 + temp2
    }
    h[0] += a; h[1] += b; h[2] += c; h[3] += d
    h[4] += e; h[5] += f; h[6] += g; h[7] += hv
  }

  val out = ByteArray(32)
  for (i in 0..7) {
    out[i * 4] = (h[i] ushr 24).toByte()
    out[i * 4 + 1] = (h[i] ushr 16).toByte()
    out[i * 4 + 2] = (h[i] ushr 8).toByte()
    out[i * 4 + 3] = h[i].toByte()
  }
  return out
}

/**
 * HMAC-SHA-256 — pure Kotlin, all platforms.
 * RFC 2104: HMAC(key, data) = SHA256((key⊕opad) ∥ SHA256((key⊕ipad) ∥ data))
 * Returns 32 bytes.
 */
internal fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
  val blockSize = 64
  val normKey = if (key.size > blockSize) sha256(key) else key
  val paddedKey = normKey + ByteArray(blockSize - normKey.size)
  val ipad = ByteArray(blockSize) { (paddedKey[it].toInt() xor 0x36).toByte() }
  val opad = ByteArray(blockSize) { (paddedKey[it].toInt() xor 0x5C).toByte() }
  return sha256(opad + sha256(ipad + data))
}

