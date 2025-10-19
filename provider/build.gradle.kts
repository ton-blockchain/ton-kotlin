plugins {
    id("ton-kotlin.project.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.tonKotlinProviderCore)
        }
    }
}
