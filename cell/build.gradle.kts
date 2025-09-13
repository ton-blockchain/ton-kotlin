import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
    signing
}


kotlin {
    explicitApi()

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
                api(libs.ktor.client.cio)
                api(project(":crypto"))
                api(project(":tl"))
                api(project(":bitstring"))
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

    coordinates(group.toString(), "ton-kotlin-cell", version.toString())

    pom {
        name = "Kotlin SDK for TON Blockchain - Cell"
        description = "A library for working with TVM Cells in TON Blockchain."
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin/"
    }
}

signing {
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
}
