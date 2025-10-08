@file:OptIn(ExperimentalAbiValidation::class)

import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationVariantSpec
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal fun KotlinProjectExtension.abiValidation(configure: AbiValidationVariantSpec.() -> Unit) =
    extensions.configure("abiValidation", configure)

kotlinExtension.abiValidation {
    enabled = true
}

// The property 'enabled' is not a part of the AbiValidationVariantSpec, so we need this bridge-method to unify enabling
private val AbiValidationVariantSpec.enabled: Property<Boolean>
    get() = when (this) {
        is AbiValidationMultiplatformExtension -> this.enabled
        is AbiValidationExtension -> this.enabled
        else -> error("Unexpected type: ${this::class.qualifiedName}")
    }
