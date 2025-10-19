plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinBlockchain)
                api(projects.tonKotlinTvm)
            }
        }
    }
}
