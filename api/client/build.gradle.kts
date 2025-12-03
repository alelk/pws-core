plugins {
  alias(libs.plugins.kotlinMultiplatform)
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
    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.property)
      implementation(libs.ktor.clientMockJvm)
      implementation(project(":domain:domain-test-fixtures"))
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
