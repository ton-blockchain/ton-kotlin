rootProject.name = "ton-kotlin"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlinx/maven")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.1.20"
        kotlin("plugin.serialization") version "1.9.22"
    }
}

System.setProperty("idea.active", "false")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

submodule("crypto")
submodule("bitstring")
submodule("tl")
submodule("bigint")
submodule("tvm")
submodule("tlb")
submodule("hashmap-tlb")
submodule("block-tlb")
submodule("tonapi-tl")
submodule("liteapi-tl")
submodule("adnl")
submodule("liteclient")
submodule("contract")

//include(":ton-kotlin-adnl")
//include(":ton-kotlin-api")
//include(":ton-kotlin-bigint")
//include(":ton-kotlin-bitstring")
//include(":ton-kotlin-block")
//include(":ton-kotlin-boc")
//include(":ton-kotlin-cell")

//include(":ton-kotlin-hashmap")
//include(":ton-kotlin-liteapi")
//include(":ton-kotlin-liteclient")
//include(":ton-kotlin-logger")
//include(":ton-kotlin-mnemonic")
//
//
//include(":ton-kotlin-contract")
//include(":ton-kotlin-tl")
//include(":ton-kotlin-tlb")
//include(":ton-kotlin-fift")

//include(":ton-kotlin-rldp")
//include(":ton-kotlin-experimental")
//include(":ton-kotlin-dht")

include(":examples")

fun submodule(name: String) {
    include(":ton-kotlin-$name")
    project(":ton-kotlin-$name").projectDir = file(name)
}
