plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
    signing
}

kotlin {
    jvm()

    macosArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.network)
            api(project(":crypto"))
            api(project(":adnl"))
            api(project(":tl"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
