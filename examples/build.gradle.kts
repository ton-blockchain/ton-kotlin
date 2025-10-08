plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinLiteclient)
                api(projects.tonKotlinContract)
            }
        }
    }
}
