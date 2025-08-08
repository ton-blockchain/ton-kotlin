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
                api(libs.kotlinx.io.bytestring)
                implementation(libs.kotlinx.crypto.sha2)
                implementation(project(":adnl"))
                implementation(project(":dht"))
                implementation(project(":overlay"))
                implementation(project(":crypto"))
                implementation(project(":tl"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.network)
                implementation(libs.kotlin.test)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "ton-kotlin-blockchain", version.toString())

    pom {
        name = "Kotlin SDK for TON Blockchain - Blockchain"
        description = "A library for working with Blockchain protocol in TON Blockchain."
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin/"
    }
}

signing {
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
}
