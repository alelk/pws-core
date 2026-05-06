package io.github.alelk.pws.portable.serialization

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual fun aesEncrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
  val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
  return cipher.doFinal(plaintext)
}

internal actual fun aesDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
  val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
  return cipher.doFinal(ciphertext)
}

internal actual fun randomBytes(size: Int): ByteArray =
  ByteArray(size).also { SecureRandom().nextBytes(it) }
