plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.data"

kotlin {
  jvm()

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {

    commonMain.dependencies {
        api(project(":domain"))
        implementation(project(":data:db-room"))
      }

    commonTest.dependencies {
        implementation(project(":data:db-room:db-room-test-fixtures"))
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.framework.engine)
        implementation(libs.kotest.property)
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
  }
}