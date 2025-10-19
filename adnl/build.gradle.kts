plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTl)
                api(projects.tonKotlinCrypto)
                api(libs.ktor.network)
                api(libs.coroutines.core)
            }
        }
    }
}
