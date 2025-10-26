plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinLiteapiTl)
                api(projects.tonKotlinBlockTlb)
                api(projects.tonKotlinTonapiTl)
                implementation(libs.serialization.json)
                implementation(libs.atomicfu)
                implementation(libs.coroutines.core)
                implementation(libs.ktor.utils)
                implementation(libs.ktor.network)
                implementation(projects.tonSdkTl)
            }
        }
        jvmTest {
            dependencies {
                api(projects.tonKotlinLiteapiTl)
                api(projects.tonKotlinBlockTlb)
                implementation(libs.atomicfu)
            }
        }
    }
}
