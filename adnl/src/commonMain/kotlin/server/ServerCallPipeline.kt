package org.ton.kotlin.adnl.server

import io.ktor.util.pipeline.*

open class ServerCallPipeline(
    final override val developmentMode: Boolean = false,
) : Pipeline<Unit, PipelineCall>(
    Setup,
    Monitoring,
    Plugins,
    Call,
    Fallback
) {
    companion object ServerPhase {
        val Setup: PipelinePhase = PipelinePhase("Setup")

        val Monitoring: PipelinePhase = PipelinePhase("Monitoring")

        val Plugins: PipelinePhase = PipelinePhase("Plugins")

        val Call: PipelinePhase = PipelinePhase("Call")

        val Fallback: PipelinePhase = PipelinePhase("Fallback")
    }
}
