plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTvm)
                implementation(libs.atomicfu)
                implementation(kotlin("reflect"))
            }
        }
    }
}
