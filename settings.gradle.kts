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

submodule("ton-sdk-bigint", path = "bigint")
submodule("ton-sdk-bitstring", path = "bitstring")
submodule("ton-sdk-tl", path = "tl")
submodule("ton-sdk-crypto", path = "crypto")
submodule("ton-sdk-blockchain", path = "blockchain")
submodule("ton-sdk-cell", path = "cell")
submodule("ton-sdk-toncenter-client", path = "ton-sdk-toncenter/ton-sdk-toncenter-client")
submodule("ton-sdk-toncenter-protocol", path = "ton-sdk-toncenter/ton-sdk-toncenter-protocol")
submodule("ton-sdk-toncenter-provider", path = "ton-sdk-toncenter/ton-sdk-toncenter-provider")


submodule("ton-kotlin-bitstring", path = "bitstring-legacy")
submodule("ton-kotlin-tl-legacy", path = "tl-legacy")
submodule("ton-kotlin-tvm", path = "tvm")
submodule("ton-kotlin-tlb", path = "tlb")
submodule("ton-kotlin-hashmap-tlb", path = "hashmap-tlb")
submodule("ton-kotlin-block-tlb", path = "block-tlb")
submodule("ton-kotlin-tonapi-tl", path = "tonapi-tl")
submodule("ton-kotlin-liteapi-tl", path = "liteapi-tl")
submodule("ton-kotlin-liteclient", path = "liteclient")
submodule("ton-kotlin-contract", path = "contract")
submodule("ton-kotlin-dict", path = "dict")

//submodule("examples-kotlin-gradle-project", path = "examples/kotlin-gradle-project", "sdk")

//submodule("provider", group = "sdk")
//submodule("provider-core", "provider/core", group = "sdk")
//submodule("provider-liteapi", "provider/liteapi", group = "sdk")
//submodule("provider-toncenter", "provider/toncenter", group = "sdk")

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

fun submodule(name: String, path: String = name) {
    include(":$name")
    project(":$name").projectDir = file(path)
}
