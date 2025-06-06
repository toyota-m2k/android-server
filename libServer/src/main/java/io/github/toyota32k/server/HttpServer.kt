package io.github.toyota32k.server

import io.github.toyota32k.logger.UtLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class HttpServer(routes:Array<Route>) : AutoCloseable {
    companion object {
        val logger = UtLog("SVR", null, this::class.java)
    }
    val httpProcessor = HttpProcessor(routes)

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
        logger.debug()
        if(serverLooper!=null) throw IllegalStateException("http server is already running.")
        val looper = Looper().apply { serverLooper = this }
        CoroutineScope(Dispatchers.IO).launch {
            serverState.value = State.RUNNING
            looper.loop(port, httpProcessor)
            serverState.value = State.STOPPED
        }
    }

    fun stop() {
        logger.debug()
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