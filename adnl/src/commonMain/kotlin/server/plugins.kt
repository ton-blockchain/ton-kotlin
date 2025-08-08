package org.ton.kotlin.adnl.server

import io.ktor.util.*
import io.ktor.util.debug.*
import io.ktor.util.pipeline.*
import org.ton.kotlin.adnl.server.debug.ijDebugReportHandlerFinished
import org.ton.kotlin.adnl.server.debug.ijDebugReportHandlerStarted


fun createServerPlugin(
    name: String,
    body: PluginBuilder<Unit>.() -> Unit
): ServerPlugin<Unit> = createServerPlugin(name, {}, body)

fun <PluginConfig : Any> createServerPlugin(
    name: String,
    createConfiguration: () -> PluginConfig,
    body: PluginBuilder<PluginConfig>.() -> Unit
) = object : ServerPlugin<PluginConfig> {
    override val key: AttributeKey<PluginInstance> = AttributeKey(name)

    override fun install(
        pipeline: Server,
        configure: PluginConfig.() -> Unit
    ): PluginInstance {
        return createPluginInstance(pipeline, pipeline, body, createConfiguration, configure)
    }
}

interface Plugin<
        in TPipeline : Pipeline<*, PipelineCall>,
        out TConfiguration : Any,
        TPlugin : Any
        > {
    val key: AttributeKey<TPlugin>

    fun install(pipeline: TPipeline, configure: TConfiguration.() -> Unit): TPlugin
}

interface BaseServerPlugin<
        in TPipeline : Pipeline<*, PipelineCall>,
        out TConfiguration : Any,
        TPlugin : Any
        > : Plugin<TPipeline, TConfiguration, TPlugin>

interface ServerPlugin<out TConfiguration : Any> :
    BaseServerPlugin<Server, TConfiguration, PluginInstance>

class PluginInstance internal constructor(internal val builder: PluginBuilder<*>)

private fun <
        PipelineT : ServerCallPipeline,
        PluginConfigT : Any
        > Plugin<PipelineT, PluginConfigT, PluginInstance>.createPluginInstance(
    application: Server,
    pipeline: ServerCallPipeline,
    body: PluginBuilder<PluginConfigT>.() -> Unit,
    createConfiguration: () -> PluginConfigT,
    configure: PluginConfigT.() -> Unit
): PluginInstance {
    val config = createConfiguration().apply(configure)

    val currentPlugin = this
    val pluginBuilder = object : PluginBuilder<PluginConfigT>(currentPlugin.key) {
        override val application: Server = application
        override val pipeline: ServerCallPipeline = pipeline
        override val pluginConfig: PluginConfigT = config
    }

    pluginBuilder.setupPlugin(body)
    return PluginInstance(pluginBuilder)
}

abstract class PluginBuilder<PluginConfig : Any>(
    internal val key: AttributeKey<PluginInstance>
) {
    abstract val application: Server
    abstract val pluginConfig: PluginConfig
    internal abstract val pipeline: ServerCallPipeline

    internal val callInterceptions: MutableList<CallInterception> = mutableListOf()

    fun onCall(block: suspend OnCallContext<PluginConfig>.(call: PipelineCall) -> Unit) {
        onDefaultPhase(
            callInterceptions,
            ServerCallPipeline.Plugins,
            "onCall",
            ::OnCallContext
        ) { call, _ ->
            block(call)
        }
    }

    private fun <T : Any, ContextT : CallContext<PluginConfig>> onDefaultPhaseWithMessage(
        interceptions: MutableList<Interception<T>>,
        phase: PipelinePhase,
        handlerName: String,
        contextInit: (pluginConfig: PluginConfig, PipelineContext<T, PipelineCall>) -> ContextT,
        block: suspend ContextT.(PipelineCall, T) -> Unit
    ) {
        interceptions.add(
            Interception(
                phase,
                action = { pipeline ->
                    pipeline.intercept(phase) {
                        // Information about the plugin name is needed for the Intellij Idea debugger.
                        val key = this@PluginBuilder.key
                        val pluginConfig = this@PluginBuilder.pluginConfig
                        addToContextInDebugMode(key.name) {
                            ijDebugReportHandlerStarted(pluginName = key.name, handler = handlerName)

                            // Perform current plugin's handler
                            contextInit(pluginConfig, this@intercept).block(call, subject)

                            ijDebugReportHandlerFinished(pluginName = key.name, handler = handlerName)
                        }
                    }
                }
            )
        )
    }

    private fun <T : Any, ContextT : CallContext<PluginConfig>> onDefaultPhase(
        interceptions: MutableList<Interception<T>>,
        phase: PipelinePhase,
        handlerName: String,
        contextInit: (pluginConfig: PluginConfig, PipelineContext<T, PipelineCall>) -> ContextT,
        block: suspend ContextT.(call: PipelineCall, body: T) -> Unit
    ) {
        onDefaultPhaseWithMessage(interceptions, phase, handlerName, contextInit) { call, body -> block(call, body) }
    }
}

private fun <Configuration : Any, Builder : PluginBuilder<Configuration>> Builder.setupPlugin(
    body: Builder.() -> Unit
) {
    apply(body)

    callInterceptions.forEach {
        it.action(pipeline)
    }
}
