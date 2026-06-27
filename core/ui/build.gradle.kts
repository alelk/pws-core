plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "io.github.alelk.pws.core"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        outputModuleName = "pws-core-ui"
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
        }
        commonTest.dependencies {
        }
    }
}
