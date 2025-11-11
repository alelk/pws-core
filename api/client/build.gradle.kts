plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

group = "io.github.alelk.pws.api"

kotlin {
  jvm()
  js(IR) {
    outputModuleName = "pws-api-client"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.api.contract)
      implementation(projects.api.mapping)
      implementation(libs.pws.domain)

      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.jsonSerialization)
      implementation(libs.ktor.client.resources)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.logging)

      implementation(libs.koin.compose)

      implementation(compose.runtime)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }
    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }
  }
}
