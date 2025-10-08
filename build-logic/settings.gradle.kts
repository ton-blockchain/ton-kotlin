dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

dependencyResolutionManagement {
    // Additional repositories for build-logic
    @Suppress("UnstableApiUsage")
    repositories {
        gradlePluginPortal()
    }
}
