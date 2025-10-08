plugins {
    id("ton-kotlin.project.library")
    id("ton-kotlin.target.js")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.json)
                api(libs.kotlinx.io.core)
            }
        }
    }
}
