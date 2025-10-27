plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api("org.ton.sdk:ton-sdk-toncenter-client:0.6.0-SNAPSHOT")
            }
        }
    }
}
