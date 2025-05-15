plugins {
    id("ton-kotlin-multiplatform")
    id("ton-kotlin-publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.tonKotlinBigint)
                api(projects.tonKotlinTl)
                api(projects.tonKotlinBitstring)
                api(projects.tonKotlinTlb)
                api(libs.datetime)
            }
        }
    }
}
