plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

group = "io.github.alelk.pws.api"

kotlin {
  jvm()

  js(IR) {
    outputModuleName = "pws-api-mapping"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.api.contract)
      api(libs.pws.domain)

      implementation(compose.runtime)
    }
  }
}

publishing {
  publications.withType(MavenPublication::class.java).configureEach {
    artifactId = "pws-api-mapping"
  }
}