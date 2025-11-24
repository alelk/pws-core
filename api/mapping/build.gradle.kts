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
      api(projects.api.contract)
      api(libs.pws.domain)
      implementation(libs.kotlinx.serialization.json)
    }
  }
}