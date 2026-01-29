import de.comahe.i18n4k.gradle.plugin.i18n4k

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.i18n4k)
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
    outputModuleName = "pws-domain-lyric-format"
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(project(":domain"))
      implementation(libs.kudzu.core)
      implementation(libs.i18n4k.core)
    }

    commonTest.dependencies {
      implementation(project(":domain:domain-test-fixtures"))
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.property)
      implementation(libs.kotest.assertions.core)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }
  }
}

i18n4k {
  packageName = "io.github.alelk.pws.domain.lyric.format.i18n"
  sourceCodeLocales = listOf("en", "uk", "ru")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

