plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.androidKmpLibrary)
}

group = "io.github.alelk.pws.core"

kotlin {
  android {
    namespace = "io.github.alelk.pws.core.navigation"
    compileSdk = rootProject.extra["androidSdkVersion"] as Int
    minSdk = 23
  }

  jvm()

  js(IR) {
    outputModuleName = "pws-core-navigation"
    browser()
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
