package io.github.toyota32k.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException


class HttpServer(routes:Array<Route>) : AutoCloseable {
    val httpProcessor = HttpProcessor(routes).apply {
        routes.forEach { this.addRoute(it) }
    }


    var serverLooper: Looper? = null
        get() = synchronized(this) { field }
        set(v) = synchronized(this) { field = v }

    enum class State {
        INITIAL,
        RUNNING,
        STOPPING,
        STOPPED,
    }
    val serverState = MutableStateFlow(State.INITIAL)

    fun start(port:Int) {
        if(serverLooper!=null) throw IllegalStateException("http server is already running.")
        val looper = Looper().apply { serverLooper = this }
        CoroutineScope(Dispatchers.IO).launch {
            serverState.value = State.RUNNING
            looper.loop(port, httpProcessor)
            serverState.value = State.STOPPED
        }
    }

    fun stop() {
        val looper = serverLooper ?: return
        serverState.value = State.STOPPING
        serverLooper = null
        CoroutineScope(Dispatchers.IO).launch {
            looper.stop()
        }
    }

    override fun close() {
        stop()
    }
}