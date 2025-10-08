import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("ton-kotlin.base")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
    }

    jvmToolchain(8)
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        compilerOptions {
            jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        }
    }

    nativeTargets()

    configureSourceSetsLayout()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("posix") {
                withMingw()
                group("nix") {
                    withLinux()
                    withApple()
                }
            }
            group("nonApplePosix") {
                withLinux()
                withMingw()
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSetsLayout() {
    sourceSets
        .matching { it.name !in listOf("main", "test") }
        .all {
            val srcDir = if (name.endsWith("Main")) "src" else "test"
            val resourcesDir = if (name.endsWith("Test")) "testResources" else "resources"
            val platform = name.dropLast(4)
            val suffix = if (platform == "common") "" else "@$platform"
            kotlin.setSrcDirs(listOf("$srcDir$suffix"))
            resources.setSrcDirs(listOf("$resourcesDir$suffix"))
        }
}

fun KotlinMultiplatformExtension.nativeTargets() {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()

//    watchosDeviceArm64() // com.ionspin.kotlin:bignum don't have watchosDeviceArm64

    linuxX64()
    linuxArm64()

    macosX64()
    macosArm64()

    mingwX64()
}
