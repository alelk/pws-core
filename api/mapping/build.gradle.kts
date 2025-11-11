plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
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
      implementation(projects.api.contract)
      implementation(libs.pws.domain)

      implementation(compose.runtime)
    }
  }
}