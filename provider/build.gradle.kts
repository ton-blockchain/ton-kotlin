plugins {
    id("ton-kotlin.project.library")
}

group = "org.ton.sdk"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.tonSdkProviderCore)
        }
    }
}
