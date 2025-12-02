
plugins {
  id("maven-publish")

  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinSerialization) apply false
  alias(libs.plugins.androidLibrary) apply false
  id("com.google.devtools.ksp") version "${libs.versions.kotlin.get()}-${libs.versions.ksp.get()}" apply false
}

val androidSdkVersion by extra(35)

val versionName by extra(
  runCatching {
    checkNotNull(
      File("app.version")
        .readText()
        .lines()
        .firstOrNull()?.trim()
        ?.takeIf { it.isNotBlank() }
    ) { "app.version empty" }
  }.getOrElse { "0.0.1-SNAPSHOT" }
)

allprojects {
  group = "io.github.alelk.pws"
  version = versionName
}

subprojects {
  apply(plugin = "maven-publish")

  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }

  publishing {
    repositories {
      mavenLocal {
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