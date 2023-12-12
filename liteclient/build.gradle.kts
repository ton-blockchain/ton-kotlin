plugins {
    id("multiplatform")
    id("publish")
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
    }
}
