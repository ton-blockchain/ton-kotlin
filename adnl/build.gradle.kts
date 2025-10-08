plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTonapiTl)
                implementation(libs.serialization.json)
                implementation(libs.atomicfu)
                implementation(libs.datetime)
                implementation(libs.coroutines.core)
                implementation(libs.ktor.utils)
                implementation(libs.ktor.network)
            }
        }
    }
}
