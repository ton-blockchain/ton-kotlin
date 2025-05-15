plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.ktor.utils)
                api(libs.sha2)
                api(libs.aes)
                api(libs.crc32)
                api(libs.pbkdf2)
                api(libs.hmac)
                implementation(libs.curve25519)
                implementation(libs.serialization.core)
            }
        }
    }
}
