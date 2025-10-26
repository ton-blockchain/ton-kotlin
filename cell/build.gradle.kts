plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonSdkCrypto)
                api(projects.tonSdkBitstring)
                api(projects.tonKotlinBigint)
                api(libs.kotlinx.io.bytestring)
                implementation(libs.serialization.core)
            }
        }
    }
}
