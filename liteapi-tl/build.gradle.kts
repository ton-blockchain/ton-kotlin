plugins {
    id("ton-kotlin.project.library")
    id("kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinTonapiTl)
                api(projects.tonKotlinBlockTlb) //TODO: remove dependency
            }
        }
    }
}
