plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    compilerOptions {
        suppressWarnings.set(true)
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonSdkBigint)
                api(projects.tonKotlinTlLegacy)
                api(projects.tonKotlinBitstring)
                api(projects.tonKotlinTlb)
                api(libs.datetime)
            }
        }
    }
}
