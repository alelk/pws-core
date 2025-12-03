plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.alelk.pws.api.client"

kotlin {
  jvm()
  js(IR) { browser(); nodejs() }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":api:client"))
        implementation(libs.koin.core)
      }
    }
    val jvmMain by getting {}
    val jsMain by getting {}
  }
}

