plugins {
    id("ton-kotlin.project.library")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinBitstring)
                api(projects.tonKotlinBigint)
                api(projects.tonKotlinCrypto)
                implementation(libs.serialization.json)
                api(libs.kotlinx.io.core)
                api(libs.ktor.utils)
            }
        }
    }
}
