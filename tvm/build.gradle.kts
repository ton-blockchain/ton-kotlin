plugins {
    id("multiplatform")
    id("publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinBitstring)
                api(projects.tonKotlinBigint)
                api(projects.tonKotlinCrypto)
                implementation(libs.serialization.json)
                api(libs.kotlinx.io.core)
                api(libs.ktor.utils)
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
