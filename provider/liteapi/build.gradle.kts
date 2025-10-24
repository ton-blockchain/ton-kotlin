plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinProviderCore)
                api(projects.tonKotlinLiteclient)
                api(libs.ktor.network)
            }
        }
    }
}
