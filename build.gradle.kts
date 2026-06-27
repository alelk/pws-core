import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
  id("maven-publish")

  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinSerialization) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.androidKmpLibrary) apply false
  id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
}

// Kotlin/JS: keep the strict FAIL policy (the default) — it catches unintended dependency drift.
// To stop it from failing on yarn 1.x cosmetic instability, the yarn.lock committed in
// kotlin-js-store is in the clean-resolution form (after `clean`); regenerate via kotlinUpgradeYarnLock.
rootProject.plugins.withType<YarnPlugin> {
  rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.FAIL
  rootProject.the<YarnRootExtension>().reportNewYarnLock = true
}

extra["androidSdkVersion"] = 36

val versionName = runCatching {
  checkNotNull(
    File("app.version")
      .readText()
      .lines()
      .firstOrNull()?.trim()
      ?.takeIf { it.isNotBlank() }
  ) { "app.version empty" }
}.getOrElse { "0.0.1-SNAPSHOT" }

allprojects {
  version = versionName
}

subprojects {
  apply(plugin = "maven-publish")

  repositories {
    // mavenLocal()
    google()
    mavenCentral()
  }

  publishing {
    repositories {
      maven {
        name = "TestLocal"
        url = rootProject.layout.projectDirectory.file("local-repo").asFile.toURI()
      }
      maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/alelk/pws-core")
        credentials {
          username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USER") ?: "alelk"
          password = findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
    reports {
      junitXml.required.set(true)
    }
  }
}