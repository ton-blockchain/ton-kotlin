package org.ton.kotlin.adnl.server

import io.ktor.util.pipeline.*

internal class Interception<T : Any>(
    val phase: PipelinePhase,
    val action: (Pipeline<T, PipelineCall>) -> Unit
)

internal typealias CallInterception = Interception<Unit>
