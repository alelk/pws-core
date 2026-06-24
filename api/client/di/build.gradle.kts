plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.alelk.pws.api.client"

kotlin {
  jvm()
  js(IR) { browser(); nodejs() }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":api:client"))
      implementation(libs.koin.core)
    }
  }
}

