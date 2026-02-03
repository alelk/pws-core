plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.domain"

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
      api(libs.kotlinx.serialization.core)
      api(libs.kotlinx.coroutines.core)
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

    jsTest.dependencies {
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
