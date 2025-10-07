plugins {
    id("multiplatform")
    id("publish")
}

kotlin {
    sourceSets {
        applyDefaultHierarchyTemplate()

        commonMain {
            dependencies {
                api(libs.serialization.json)
                api(libs.kotlinx.io.core)
            }
        }
    }
}
