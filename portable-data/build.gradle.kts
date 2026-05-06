plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.portable"

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  sourceSets {
    commonMain.dependencies {
      implementation(project(":domain"))
      implementation(libs.kaml)
      implementation(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(project(":domain:domain-test-fixtures"))
      implementation(libs.kotest.property)
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.framework.engine)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    jvmMain.dependencies {
      // java.util.zip.GZIPOutputStream/GZIPInputStream available on JVM
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
      implementation(kotlin("test"))
    }

    // nativeMain covers iosX64, iosArm64, iosSimulatorArm64 — uses platform.zlib
    val nativeMain by creating { dependsOn(commonMain.get()) }
    val iosX64Main by getting { dependsOn(nativeMain) }
    val iosArm64Main by getting { dependsOn(nativeMain) }
    val iosSimulatorArm64Main by getting { dependsOn(nativeMain) }
  }
}