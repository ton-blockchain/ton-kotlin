import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
    signing
}


kotlin {
    jvm()

    macosArm64()
    macosX64()
    linuxArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.crypto.sha2)
                api(libs.kotlinx.io.core)
                api(libs.kotlinx.crypto.aes)
                api(libs.curve25519)
                api(libs.kotlinx.serialization.core)
                api(project(":tl"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "ton-kotlin-crypto", version.toString())

    pom {
        name = "Kotlin SDK for TON Blockchain - Crypto"
        description = "A library for working with cryptography in TON Blockchain."
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin/"
    }
}

signing {
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
}
