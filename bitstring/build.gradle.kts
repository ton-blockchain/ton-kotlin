plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.serialization.core)
                implementation(libs.kotlinx.io.bytestring)
                implementation(libs.kotlinx.io.core)
                implementation(projects.tonSdkBigint)
            }
        }
    }
}
