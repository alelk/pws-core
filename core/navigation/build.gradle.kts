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
      implementation(libs.pws.domain)

      implementation(libs.kotlinx.coroutinesCore)
      implementation(libs.voyager.core)

      implementation(compose.runtime)
    }
    commonTest.dependencies {}
  }
}
