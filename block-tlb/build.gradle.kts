plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinHashmapTlb)
                api(projects.tonKotlinTlb)
                api(projects.tonKotlinTl)
                implementation(libs.serialization.core)
            }
        }
    }
}
