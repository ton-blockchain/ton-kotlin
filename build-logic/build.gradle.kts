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
    implementation(libs.serialization.json)
    implementation(libs.mavenPublishing)
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.charleskorn.kaml:kaml:0.102.0")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        allWarningsAsErrors = true
    }
}
