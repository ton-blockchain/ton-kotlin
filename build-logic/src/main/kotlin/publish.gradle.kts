
plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    pom {
        name = project.name
        description = "Kotlin/Multiplatform SDK for The Open Network"
        inceptionYear = "2025"
        url = "https://github.com/ton-blockchain/ton-kotlin"

        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "andreypfau"
                name = "Andrey Pfau"
                email = "andreypfau@ton.org"
            }
        }
        scm {
            url = "https://github.com/ton-blockchain/ton-kotlin"
        }
    }

    signAllPublications()
}
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ton-blockchain/ton-kotlin")
            credentials {
                username = providers.environmentVariable("ORG_GRADLE_PROJECT_githubPackagesUsername").orNull
                    ?: providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("ORG_GRADLE_PROJECT_githubPackagesPassword").orNull
                    ?: providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}
