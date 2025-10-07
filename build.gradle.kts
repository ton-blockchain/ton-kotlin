import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    alias(libs.plugins.detekt) apply false
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
    apply(plugin = "io.gitlab.arturbosch.detekt")

    val baselineFile = rootProject.layout.projectDirectory.file("detekt-baselines/${project.name}.xml").asFile

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.ExperimentalUnsignedTypes")
        }
    }

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        buildUponDefaultConfig = true
        baseline = baselineFile
        config.from(rootProject.files("detekt.yml"))
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        setSource(files("src"))
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "17"
        setSource(files("src"))
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**")
        baseline.set(rootProject.layout.projectDirectory.file("detekt-baselines/${project.name}.xml"))
        doFirst { baseline.get().asFile.parentFile?.mkdirs() }
    }
}
