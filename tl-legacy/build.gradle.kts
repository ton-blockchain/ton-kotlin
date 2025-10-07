plugins {
    id("multiplatform")
    id("publish")
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
