plugins {
    id("ton-kotlin.base")
}

println("Build version: ${project.version}")
println("Kotlin version: ${libs.versions.kotlin.get()}")

allprojects {
    repositories {
        mavenCentral()
    }
}
