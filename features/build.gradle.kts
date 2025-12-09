plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

group = "io.github.alelk.pws.features"

kotlin {
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
      implementation(libs.voyager.koin)

      implementation(libs.koin.core)
      implementation(libs.koin.compose)

      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    jvmMain.dependencies {}
  }
}