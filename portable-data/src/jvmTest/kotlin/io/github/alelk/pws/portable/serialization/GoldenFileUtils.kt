package io.github.alelk.pws.portable.serialization

import java.io.File
import java.net.URL

/** Resolves the golden directory in the source tree (not the build output). */
internal fun goldenDir(): File {
  val resourceRoot: URL = object {}::class.java.getResource("/") ?: error("Cannot find resource root")
  // build/classes/kotlin/jvm/test → 5 steps up → module root (portable-data/)
  var dir = File(resourceRoot.toURI())
  repeat(5) { dir = dir.parentFile }
  return dir.resolve("src/jvmTest/resources/golden")
}
