plugins {
    id("multiplatform")
    id("publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinHashmapTlb)
                api(projects.tonKotlinTlb)
                api(projects.tonKotlinTlLegacy)
                implementation(libs.serialization.core)
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
