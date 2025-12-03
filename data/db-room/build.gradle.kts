import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("com.google.devtools.ksp")
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotestMultiplatform)
}

group = "io.github.alelk.pws.data"

kotlin {
  androidTarget()
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

    androidUnitTest.dependencies {
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

  targets.withType<KotlinAndroidTarget> {
    ksp {
      arg("room.generateKotlin", "true")
    }
  }

  // fixme:
  // Kotest native IR plugin is currently incompatible with Kotlin 2.2.21 in this project,
  // which fails native test compilations (compileTestKotlinIos*). Disable native test
  // compilation for this module to allow the build to succeed while keeping JVM/Android tests.
  targets.withType<KotlinNativeTarget>().configureEach {
    compilations.named("test") {
      compileTaskProvider.configure { enabled = false }
    }
  }
}

dependencies {
  add("kspAndroid", libs.room.compiler)
  add("kspJvm", libs.room.compiler)
  add("kspIosSimulatorArm64", libs.room.compiler)
  add("kspIosX64", libs.room.compiler)
  add("kspIosArm64", libs.room.compiler)
}

android {
  compileSdk = rootProject.extra["androidSdkVersion"] as Int

  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  lint {
    targetSdk = rootProject.extra["androidSdkVersion"] as Int
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
    getByName("debug") {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  namespace = "io.github.alelk.pws.data.room_database"
  ksp {
    arg("room.generateKotlin", "true")
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }

    singleVariant("debug") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}