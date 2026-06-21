plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

group = "io.github.alelk.pws.data"

kotlin {
  jvm()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(project(":data:db-room"))
      api(project(":domain"))
      api(project(":domain:domain-test-fixtures"))
      api(libs.kotest.property)
      api(libs.room.runtime)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines.core)
    }
  }
}