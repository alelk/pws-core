@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

compose.resources {
  publicResClass = true
  packageOfResClass = "io.github.alelk.pws.features.resources"
}

group = "io.github.alelk.pws.features"

kotlin {
  android {
    namespace = "io.github.alelk.pws.features"
    compileSdk = rootProject.extra["androidSdkVersion"] as Int
    minSdk = 23
    androidResources {
      enable = true
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()
  js(IR) {
    outputModuleName = "pws-features"
    browser()
  }

  // Custom intermediate groups on top of the default hierarchy template:
  //   mobileMain (android + ios) and skikoMain (jvm + js).
  // The com.android.kotlin.multiplatform.library target is not a KotlinAndroidTarget, so
  // withAndroidTarget() never matches it — select it by platform type (androidJvm) instead.
  applyDefaultHierarchyTemplate {
    common {
      group("mobile") {
        withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
        withIos()
      }
      group("skiko") {
        withJvm()
        withJs()
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:navigation"))
      implementation(project(":domain"))
      implementation(project(":domain:lyric-format"))

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.materialIconsExtended)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.components.uiToolingPreview)

      implementation(libs.kotlinx.coroutines.core)

      implementation(libs.voyager.navigator)
      implementation(libs.voyager.tab.navigator)
      implementation(libs.voyager.koin)
      implementation(libs.voyager.transitions)

      implementation(libs.koin.core)
      implementation(libs.koin.compose)

      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }

    commonTest.dependencies {
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.kotest.framework.engine)
      implementation(libs.kotest.assertions.core)
      implementation(kotlin("test-common"))
      implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
      implementation(libs.kotest.runner.junit5)
    }

    androidMain.dependencies {
      implementation(libs.koin.android)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

