plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        extraWarnings.set(true)
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io.bytestring)
                api(projects.tonSdkTl)
                implementation(libs.serialization.core)
            }
        }
    }
}
