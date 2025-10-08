plugins {
    id("ton-kotlin.project.library")
}

kotlin {
    sourceSets {
        nativeMain {
            dependencies {
                implementation(libs.bignum)
            }
        }
    }
}
