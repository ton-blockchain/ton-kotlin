plugins {
    id("ton-kotlin.project.library")
}

group = "org.ton.sdk"

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        extraWarnings.set(true)
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonSdkTl)
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
