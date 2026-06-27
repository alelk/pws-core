plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.kotest)
  id("com.google.devtools.ksp")
}

group = "io.github.alelk.pws.data"

kotlin {
  android {
    namespace = "io.github.alelk.pws.data.repo_room"
    compileSdk = rootProject.extra["androidSdkVersion"] as Int
    minSdk = 23

    withHostTestBuilder {
      sourceSetTreeName = "test"
    }
  }

  jvm()

  iosArm64()
  iosSimulatorArm64()

  sourceSets {

    commonMain.dependencies {
        api(project(":domain"))
        implementation(project(":domain:lyric-format"))
        implementation(project(":data:db-room"))
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.arrow.core)
      }

    commonTest.dependencies {
        implementation(project(":data:db-room:db-room-test-fixtures"))
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.framework.engine)
        implementation(libs.kotest.property)
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }

    jvmTest.dependencies {
        implementation(libs.kotest.runner.junit5)
      }

    getByName("androidHostTest").dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.property)
      }
  }
}

dependencies {
  add("kspAndroid", libs.room.compiler)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

