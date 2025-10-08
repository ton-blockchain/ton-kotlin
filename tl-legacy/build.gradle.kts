plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinCrypto)
                api(libs.serialization.json)
                api(libs.kotlinx.io.core)
            }
        }
    }
}
