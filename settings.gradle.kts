rootProject.name = "pws-core"


pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("libs.versions.toml"))
    }
  }
}

include(
  ":domain",
  ":domain:domain-test-fixtures",

  ":backup",

  ":data:db-room",
  ":data:db-room:db-room-test-fixtures",
  ":data:repo-room"
)