import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
}

allprojects {
    group = "org.ton"
    version = "0.4.3"

    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/service/local/repositories/releases/content")
    }
}

subprojects {
    apply(plugin = "kotlinx-serialization")

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.ExperimentalUnsignedTypes")
        }
    }
}
