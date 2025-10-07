
plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
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
