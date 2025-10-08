plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization)
    implementation(libs.mavenPublishing)
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        allWarningsAsErrors = true
    }
}
