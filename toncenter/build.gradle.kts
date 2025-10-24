plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTl)
                api(projects.tonKotlinBlockchain)
                api(libs.serialization.json)
                api(libs.ktor.client.core)
            }
        }
        commonTest {
            dependencies {
                api(libs.ktor.client.cio)
                implementation(libs.coroutines.test)
            }
        }
    }
}
