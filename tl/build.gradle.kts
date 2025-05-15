plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinCrypto)
                api(projects.tonKotlinBitstring)
                api(libs.ktor.utils)
                api(libs.serialization.json)
                api(libs.kotlinx.io)
            }
        }
    }
}
