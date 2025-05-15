import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import kotlin.jvm.optionals.getOrNull

plugins {
    kotlin("multiplatform")
}

kotlin {
//    explicitApiWarning()
    explicitApi()

    compilerOptions {
//        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    val versionCatalog: VersionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    jvmToolchain {
        val javaVersion = versionCatalog.findVersion("java").getOrNull()?.requiredVersion
            ?: throw GradleException("Version 'java' is not specified in the version catalog")
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    jvm {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xjvm-default=all")
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlin.io.encoding.ExperimentalEncodingApi")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    configureNativePlatforms()
    sourceSets.configureEach {
        configureSourceSet()
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("native") {
                group("nativeNonApple") {
                    group("mingw")
                    group("unix") {
                        group("linux")
                        group("androidNative")
                    }
                }

                group("nativeNonAndroid") {
                    group("apple")
                    group("mingw")
                    group("linux")
                }
            }
            group("nodeFilesystemShared") {
                withJs()
                withWasmJs()
            }
            group("wasm") {
                withWasmJs()
                withWasmWasi()
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureNativePlatforms() {
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

// com.ionspin.kotlin:bignum not supporting watchosDeviceArm64
//    watchosDeviceArm64() 

//    androidNativeArm32()
//    androidNativeArm64()
//    androidNativeX64()
//    androidNativeX86()

    macosX64()
    macosArm64()

    linuxArm64()
    linuxX64()

    mingwX64()
}

fun KotlinSourceSet.configureSourceSet() {
    val srcDir = if (name.endsWith("Main")) "src" else "test"
    val platform = name.dropLast(4)
    kotlin.srcDir("$platform/$srcDir")
    if (name == "jvmMain") {
        resources.srcDir("$platform/resources")
    } else if (name == "jvmTest") {
        resources.srcDir("$platform/test-resources")
    }
}
