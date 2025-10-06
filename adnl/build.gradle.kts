plugins {
    id("multiplatform")
    id("publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTonapiTl)
                implementation(libs.serialization.json)
                implementation(libs.atomicfu)
                implementation(libs.datetime)
                implementation(libs.coroutines.core)
                implementation(libs.ktor.utils)
            }
        }
        nativeMain {
            dependencies {
                implementation(libs.bignum)
            }
        }
        appleMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }
        linuxMain {
            dependencies {
                implementation(libs.ktor.network)
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
