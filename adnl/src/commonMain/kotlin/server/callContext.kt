package org.ton.kotlin.adnl.server

import io.ktor.util.pipeline.*

open class CallContext<PluginConfig : Any> internal constructor(
    val pluginConfig: PluginConfig,
    protected open val context: PipelineContext<*, PipelineCall>
) {
    // Internal usage for tests only
    internal fun finish() = context.finish()
}

class OnCallContext<PluginConfig : Any> internal constructor(
    pluginConfig: PluginConfig,
    context: PipelineContext<Unit, PipelineCall>
) : CallContext<PluginConfig>(pluginConfig, context)
