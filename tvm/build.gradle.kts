plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinBitstring)
                api(projects.tonKotlinBigint)
                api(projects.tonKotlinCrypto)
                implementation(libs.serialization.json)
                api(libs.kotlinx.io)
            }
        }
    }
}
