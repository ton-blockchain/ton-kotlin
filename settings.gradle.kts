pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ton-kotlin"
include(":adnl")
include(":crypto")
include(":tl")
include(":dht")
include(":overlay")
include(":fec")
include(":rldp")
include(":http")
include(":blockchain")
