plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.api"

kotlin {
  jvm()
  js(IR) {
    outputModuleName = "pws-api-client"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(project(":api:contract"))
      implementation(project(":api:mapping"))
      api(project(":domain"))

      api(libs.ktor.client.core)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.ktor.client.resources)
      implementation(libs.ktor.client.logging)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }
    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }
    commonTest.dependencies {
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.property)
      implementation(libs.ktor.clientMock)
      implementation(project(":domain:domain-test-fixtures"))
    }
    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
