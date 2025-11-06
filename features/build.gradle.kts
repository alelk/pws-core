plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  jvm()
  js(IR) {
    outputModuleName = "pws-features"
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.navigation)

      implementation(libs.pws.domain)

      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      implementation(libs.voyager.core)
      implementation(libs.voyager.koin)
      implementation(libs.koin.compose)

      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    jvmMain.dependencies {}
  }
}