plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

kotlin {
    compilerOptions {
//        allWarningsAsErrors.set(true)
//        extraWarnings.set(true)
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonSdkCrypto)
                api(projects.tonSdkBitstring)
                api(projects.tonSdkBigint)
                api(libs.kotlinx.io.bytestring)
                implementation(libs.serialization.core)
            }
        }
    }
}
