plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                api("org.ton.sdk:ton-sdk-toncenter-client-jvm:0.6.0-SNAPSHOT")
            }
        }
    }
}
