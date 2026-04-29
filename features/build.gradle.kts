plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.androidKmpLibrary)
}

compose.resources {
  publicResClass = true
  packageOfResClass = "io.github.alelk.pws.features.resources"
}

group = "io.github.alelk.pws.features"

kotlin {
  android {
    namespace = "io.github.alelk.pws.features"
    compileSdk = rootProject.extra["androidSdkVersion"] as Int
    minSdk = 23
  }

  jvm()
  js(IR) {
    outputModuleName = "pws-features"
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:navigation"))
      implementation(project(":domain"))

      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      implementation(libs.kotlinx.coroutines.core)

      implementation(libs.voyager.navigator)
      implementation(libs.voyager.tab.navigator)
      implementation(libs.voyager.koin)
      implementation(libs.voyager.transitions)

      implementation(libs.koin.core)
      implementation(libs.koin.compose)

      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    jvmMain.dependencies {}
    androidMain.dependencies {
      implementation(libs.koin.android)
    }
  }
}