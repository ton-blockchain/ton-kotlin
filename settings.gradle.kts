rootProject.name = "ton-kotlin"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlinx/maven")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.2.20"
        kotlin("plugin.serialization") version "2.2.20"
    }
}

System.setProperty("idea.active", "false")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

submodule("bigint", group = "sdk")
submodule("bitstring", group = "sdk")
submodule("tl", group = "sdk")
submodule("crypto", group = "sdk")
submodule("toncenter-client", group = "sdk")
submodule("blockchain", group = "sdk")
submodule("cell", group = "sdk")

submodule("bitstring", path = "bitstring-legacy")
submodule("tl-legacy")
submodule("tvm")
submodule("tlb")
submodule("hashmap-tlb")
submodule("block-tlb")
submodule("tonapi-tl")
submodule("liteapi-tl")
submodule("liteclient")
submodule("contract")
submodule("dict")

submodule("provider", group = "sdk")
submodule("provider-core", "provider/core", group = "sdk")
//submodule("provider-liteapi", "provider/liteapi")

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

//include(":examples")

fun submodule(name: String, path: String = name, group: String = "kotlin") {
    include(":ton-$group-$name")
    project(":ton-$group-$name").projectDir = file(path)
}
