plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTl)
                api(projects.tonSdkBlockchain)
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
