plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinCrypto)
                implementation(libs.serialization.core)
                implementation(libs.kotlinx.io)
            }
        }
    }
}
