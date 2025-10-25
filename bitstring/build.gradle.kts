plugins {
    id("ton-kotlin.project.library")
    id("ton-kotlin.target.js")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.serialization.core)
                implementation(libs.kotlinx.io.bytestring)
                implementation(projects.tonKotlinBigint)
            }
        }
    }
}
