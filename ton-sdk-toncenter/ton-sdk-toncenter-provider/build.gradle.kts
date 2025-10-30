plugins {
    id("ton-kotlin.project.library")
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

            }
        }
    }
}
