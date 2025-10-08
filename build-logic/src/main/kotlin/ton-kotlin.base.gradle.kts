version = resolveVersion()

internal fun Project.resolveVersion(): String {
    val projectVersion = project.rootDir.resolve("VERSION").readText().trim()
    val releaseVersion = providers.gradleProperty("releaseVersion").orNull

    return releaseVersion ?: projectVersion
}
