plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

group = "io.github.alelk.pws.api"

kotlin {
  jvm()

  js(IR) {
    outputModuleName = "pws-api-mapping"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(project(":api:contract"))
      api(project(":domain"))
      implementation(libs.kotlinx.serialization.json)
    }
  }
}