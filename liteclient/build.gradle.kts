plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinAdnl)
                api(projects.tonKotlinLiteapiTl)
                api(projects.tonKotlinBlockTlb)
                implementation(libs.atomicfu)
            }
        }
        jvmTest {
            dependencies {
                api(projects.tonKotlinAdnl)
                api(projects.tonKotlinLiteapiTl)
                api(projects.tonKotlinBlockTlb)
                implementation(libs.atomicfu)
            }
        }
    }
}
