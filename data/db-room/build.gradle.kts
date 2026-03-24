import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
  id("com.google.devtools.ksp")
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotest)
}

group = "io.github.alelk.pws.data"

kotlin {
  android {
    namespace = "io.github.alelk.pws.data.room_database"
    compileSdk = rootProject.extra["androidSdkVersion"] as Int
    minSdk = 23

    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
      }
    }

    withHostTestBuilder {
      sourceSetTreeName = "test"
    }

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  jvm()

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {

    all {
      languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }

    commonMain.dependencies {
      api(project(":domain"))
      api(libs.room.runtime)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(project(":domain:domain-test-fixtures"))
      implementation(project(":data:db-room:db-room-test-fixtures"))
      implementation(libs.kotest.assertions.core)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.property)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    androidMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.coroutines.android)
      runtimeOnly(libs.room.runtime)
      runtimeOnly(libs.room.ktx)
    }

    getByName("androidHostTest").dependencies {
      implementation(libs.kotest.runner.junit5)
      implementation(libs.kotest.property)
      implementation(libs.kotest.assertions.core)
      implementation(libs.androidx.test.core)
      implementation(libs.kotest.runner.android)
      implementation(libs.kotest.extensions.android)
      implementation(libs.robolectric)
    }

    jvmMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
      runtimeOnly(libs.room.runtime.jvm)
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
      implementation(libs.sqlite.bundled)
    }
  }

  // fixme:
  // Kotest native IR plugin is currently incompatible with Kotlin 2.3.0 in this project,
  // which fails native test compilations (compileTestKotlinIos*). Disable native test
  // compilation, linking and execution for this module to allow the build to succeed
  // while keeping JVM/Android tests.
  targets.withType<KotlinNativeTarget>().configureEach {
    compilations.named("test") {
      compileTaskProvider.configure { enabled = false }
    }
    binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>().configureEach {
      linkTaskProvider.configure { enabled = false }
    }
  }
}

tasks.withType<KotlinNativeTest>().configureEach {
  enabled = false
}

dependencies {
  add("kspAndroid", libs.room.compiler)
  add("kspJvm", libs.room.compiler)
  add("kspIosSimulatorArm64", libs.room.compiler)
  add("kspIosX64", libs.room.compiler)
  add("kspIosArm64", libs.room.compiler)
}

