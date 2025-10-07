plugins {
    id("multiplatform")
    id("publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io.bytestring)
                api(projects.tonKotlinTl)
                implementation(libs.serialization.core)
            }
        }

        applyDefaultHierarchyTemplate()

        val linuxAndMingw by creating {
            dependsOn(nativeMain.get())
        }

        linuxMain.get().dependsOn(linuxAndMingw)
        mingwMain.get().dependsOn(linuxAndMingw)
    }
}
