package io.github.alelk.pws.portable.serialization

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

internal actual fun gzip(data: ByteArray): ByteArray =
  ByteArrayOutputStream().use { baos ->
    GZIPOutputStream(baos).use { it.write(data) }
    baos.toByteArray()
  }

internal actual fun ungzip(data: ByteArray): ByteArray =
  GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }


