package org.ton.kotlin.adnl.server.debug

import io.ktor.util.debug.*
import io.ktor.util.debug.plugins.*

internal suspend fun ijDebugReportHandlerStarted(pluginName: String, handler: String) {
    useContextElementInDebugMode(PluginsTrace) { trace ->
        trace.eventOrder.add(PluginTraceElement(pluginName, handler, PluginTraceElement.PluginEvent.STARTED))
    }
}

internal suspend fun ijDebugReportHandlerFinished(pluginName: String, handler: String) {
    useContextElementInDebugMode(PluginsTrace) { trace ->
        trace.eventOrder.add(PluginTraceElement(pluginName, handler, PluginTraceElement.PluginEvent.FINISHED))
    }
}
