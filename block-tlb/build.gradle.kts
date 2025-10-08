plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
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
    }
}
