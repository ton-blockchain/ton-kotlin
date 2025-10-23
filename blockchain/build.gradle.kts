plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io.bytestring)
                api(projects.tonKotlinCell)
                api(projects.tonKotlinDict)
            }
        }
    }
}
