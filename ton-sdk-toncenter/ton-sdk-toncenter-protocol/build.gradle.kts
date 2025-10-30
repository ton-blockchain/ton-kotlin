plugins {
    id("ton-kotlin.project.library")
    id("ton-kotlin.openapi")
    id("kotlinx-serialization")
}

group = "org.ton.sdk"

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        extraWarnings.set(true)
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.json)
                api(libs.ktor.client.core)
            }
        }
        commonTest {
            dependencies {
                api(libs.ktor.client.cio)
                implementation(libs.coroutines.test)
            }
        }
    }
}

tasks.register<Ton_kotlin_openapi_gradle.GenerateOpenApiClientTask>("generateOpenApiClient") {
    group = "code generation"
    description = "Generates OpenAPI client code using OpenAPI Generator"

    openApiSpec.set(layout.projectDirectory.file("openapi.yaml"))
    outputDir.set(layout.projectDirectory.dir("src"))
    packageName.set("org.ton.sdk.toncenter")
    mainClassName.set("TonCenterV3")
}
