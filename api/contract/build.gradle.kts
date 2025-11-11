plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

group = "io.github.alelk.pws.api"

kotlin {
  jvm()

  js(IR) {
    outputModuleName = "pws-api-contract"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.ktor.resources)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.kotlinx.serialization.json)

      implementation(compose.runtime)
    }
  }
}