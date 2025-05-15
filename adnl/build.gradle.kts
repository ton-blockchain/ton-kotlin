plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
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
    }
}
