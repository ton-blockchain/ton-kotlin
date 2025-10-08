plugins {
    id("ton-kotlin.project.library")
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
