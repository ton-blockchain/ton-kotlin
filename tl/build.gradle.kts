plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

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
