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
  iosX64()
  iosArm64()
  iosSimulatorArm64()
  js(IR) {
    outputModuleName = "pws-features"
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:navigation"))
      implementation(project(":domain"))
      implementation(project(":domain:lyric-format"))

      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

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

    val mobileMain by creating {
      dependsOn(commonMain.get())
    }
    androidMain.get().dependsOn(mobileMain)

    val iosMain by creating {
      dependsOn(mobileMain)
    }
    iosX64Main.get().dependsOn(iosMain)
    iosArm64Main.get().dependsOn(iosMain)
    iosSimulatorArm64Main.get().dependsOn(iosMain)

    val skikoMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain.get().dependsOn(skikoMain)
    jsMain.get().dependsOn(skikoMain)

    androidMain.dependencies {
      implementation(libs.koin.android)
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

