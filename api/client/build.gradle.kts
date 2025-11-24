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
      implementation(projects.api.contract)
      implementation(projects.api.mapping)
      api(libs.pws.domain)

      api(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.jsonSerialization)
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
      implementation(libs.kotest.runner)
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.property)
      implementation(libs.ktor.clientMockJvm)
      implementation(libs.pws.domainTestFixtures)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
