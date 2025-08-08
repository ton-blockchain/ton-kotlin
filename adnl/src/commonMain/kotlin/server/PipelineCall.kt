package org.ton.kotlin.adnl.server

import io.ktor.util.pipeline.*

interface PipelineCall

inline val PipelineContext<*, PipelineCall>.call: PipelineCall get() = context
