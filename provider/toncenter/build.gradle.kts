plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonSdkProviderCore)
                api(projects.tonSdkToncenterClient)
                api(libs.ktor.network)
            }
        }
    }
}
