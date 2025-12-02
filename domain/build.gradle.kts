plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  js(IR) {
    outputModuleName = "pws-domain"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.coroutines.core)
    }

    commonTest.dependencies {
      implementation(project(":domain:domain-test-fixtures"))
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.property)
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.assertions.json)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }
  }
}