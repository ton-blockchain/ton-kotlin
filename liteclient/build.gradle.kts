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
        jvmTest {
            dependencies {
                api(projects.tonKotlinAdnl)
                api(projects.tonKotlinLiteapiTl)
                api(projects.tonKotlinBlockTlb)
                implementation(libs.atomicfu)
            }
        }

        all {
            if (name.endsWith("Main")) {
                val suffix = if (name.startsWith("common")) "" else "@${name.removeSuffix("Main")}"
                kotlin.srcDir("src$suffix")
                resources.srcDir("resources$suffix")
            }
            if (name.endsWith("Test")) {
                val suffix = if (name.startsWith("common")) "" else "@${name.removeSuffix("Test")}"
                kotlin.srcDir("test$suffix")
                resources.srcDir("testResources$suffix")
            }
        }
    }
}
