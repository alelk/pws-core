package io.github.alelk.pws.portable.serialization

/**
 * Platform-specific gzip compression/decompression.
 *
 * - JVM: `java.util.zip.GZIPOutputStream / GZIPInputStream`
 * - iOS / Native: `platform.zlib` (deflate with gzip header, wbits = 15 + 16)
 */
internal expect fun gzip(data: ByteArray): ByteArray
internal expect fun ungzip(data: ByteArray): ByteArray

