package io.github.alelk.pws.portable.serialization
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_DEFAULT_STRATEGY
import platform.zlib.Z_DEFLATED
import platform.zlib.Z_FINISH
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.ZLIB_VERSION
import platform.zlib.deflate
import platform.zlib.deflateEnd
import platform.zlib.deflateInit2_
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream
// wbits = 15 + 16 → gzip header/trailer (see zlib manual)
private const val GZIP_WBITS = 15 + 16
@OptIn(ExperimentalForeignApi::class)
internal actual fun gzip(data: ByteArray): ByteArray = memScoped {
  val stream = alloc<z_stream>()
  stream.zalloc = null
  stream.zfree = null
  stream.opaque = null
  deflateInit2_(
    stream.ptr,
    Z_DEFAULT_COMPRESSION,
    Z_DEFLATED,
    GZIP_WBITS,
    8,
    Z_DEFAULT_STRATEGY,
    ZLIB_VERSION,
    sizeOf<z_stream>().toInt()
  )
  val output = ByteArray(data.size + data.size / 10 + 64)
  data.usePinned { inputPinned ->
    output.usePinned { outputPinned ->
      stream.avail_in = data.size.toUInt()
      stream.next_in = inputPinned.addressOf(0).reinterpret()
      stream.avail_out = output.size.toUInt()
      stream.next_out = outputPinned.addressOf(0).reinterpret()
      deflate(stream.ptr, Z_FINISH)
    }
  }
  val compressedSize = (output.size.toUInt() - stream.avail_out).toInt()
  deflateEnd(stream.ptr)
  output.copyOf(compressedSize)
}
@OptIn(ExperimentalForeignApi::class)
actual fun ungzip(data: ByteArray): ByteArray = memScoped {
  val stream = alloc<z_stream>()
  stream.zalloc = null
  stream.zfree = null
  stream.opaque = null
  inflateInit2_(
    stream.ptr,
    GZIP_WBITS,
    ZLIB_VERSION,
    sizeOf<z_stream>().toInt()
  )
  var output = ByteArray(maxOf(data.size * 4, 256))
  var totalOut = 0
  var ret = Z_OK
  data.usePinned { inputPinned ->
    stream.avail_in = data.size.toUInt()
    stream.next_in = inputPinned.addressOf(0).reinterpret()
    while (ret == Z_OK) {
      if (totalOut >= output.size) output = output.copyOf(output.size * 2)
      output.usePinned { outputPinned ->
        stream.avail_out = (output.size - totalOut).toUInt()
        stream.next_out = outputPinned.addressOf(totalOut).reinterpret()
        ret = inflate(stream.ptr, Z_NO_FLUSH)
        totalOut = output.size - stream.avail_out.toInt()
      }
      if (stream.avail_out != 0u) break
    }
  }
  check(ret == Z_STREAM_END) { "ungzip failed: zlib inflate returned $ret" }
  inflateEnd(stream.ptr)
  output.copyOf(totalOut)
}
