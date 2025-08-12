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
                api(libs.base32)
                api(libs.kotlinx.io.bytestring)
                api(libs.ktor.client.cio)
                api(project(":adnl"))
                api(project(":dht"))
                api(project(":crypto"))
                implementation(project(":rldp"))
                implementation(project(":tl"))
                implementation(project(":fec"))
            }
        }
        jvmMain.dependencies {
            implementation("ch.qos.logback:logback-classic:1.5.18")
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

    coordinates(group.toString(), "ton-kotlin-http", version.toString())

    pom {
        name = "Kotlin SDK for TON Blockchain - HTTP"
        description = "A library for working with HTTP protocol in TON Blockchain."
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin/"
    }
}

signing {
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
}
