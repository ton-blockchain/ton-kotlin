plugins {
    id("multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinLiteclient)
                api(projects.tonKotlinContract)
            }
        }
    }
}
