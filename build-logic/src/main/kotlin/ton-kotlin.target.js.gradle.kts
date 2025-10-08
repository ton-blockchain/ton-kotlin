import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("ton-kotlin.kmp")
}

kotlin {
    js {
        browser()
        nodejs()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
}
