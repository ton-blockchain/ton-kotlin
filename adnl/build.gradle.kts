import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "org.ton.kotlin"
version = "2.0.0"

kotlin {
    jvm()
    iosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.network)
                implementation(project(":crypto"))
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

    coordinates(group.toString(), "adnl", version.toString())

    pom {
        name = "Kotlin SDK for TON Blockchain - ADNL"
        description = "A library for working with ADNL protocol in TON Blockchain."
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin/"
    }
}
