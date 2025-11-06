plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "io.github.alelk.pws.core"

kotlin {
    jvm()

    js(IR) {
        outputModuleName = "pws-core-ui"
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
        }
        commonTest.dependencies {
        }
    }
}
