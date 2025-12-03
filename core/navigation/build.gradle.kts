plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

group = "io.github.alelk.pws.core"

kotlin {
  jvm()

  js(IR) {
    outputModuleName = "pws-core-navigation"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":domain"))

      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.voyager.core)

      implementation(compose.runtime)
    }
    commonTest.dependencies {}
  }
}
