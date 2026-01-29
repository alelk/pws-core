plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.backup"

kotlin {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  sourceSets {
    commonMain.dependencies {
      implementation(project(":domain"))
      implementation(libs.kaml)
      implementation(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(project(":domain:domain-test-fixtures"))
      implementation(libs.kotest.property)
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.framework.engine)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }
  }
}