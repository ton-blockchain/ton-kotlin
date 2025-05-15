plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.serialization.json)
            }
        }
        nativeMain {
            dependencies {
                implementation(libs.bignum)
            }
        }
    }
}
