package io.github.toyota32k.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.net.ServerSocket

class Looper : AutoCloseable {
    var listener: ServerSocket? = null
        get() = synchronized(this) { field }
        set(v) = synchronized(this) { field = v }

    var alive = true



    suspend fun loop(port: Int, clientHandler: IClientHandler) {
        if(listener!=null) throw IllegalStateException("looper is already running.")

        val listener = withContext(Dispatchers.IO) {
            ServerSocket(port).apply { listener = this }
        }
        this@Looper.listener = listener

        try {
            while (alive) {
                val socket = withContext(Dispatchers.IO) {
                    listener.accept()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        clientHandler.handleClient(socket)
                    } catch(_:Throwable) {
                        // 個々のコネクションのエラーは無視して処理継続
                    }
                }
            }
        } catch(e:Throwable) {
            alive = false
        }

        withContext(Dispatchers.IO) {
            listener.close()
            this@Looper.listener = null
        }
    }

    suspend fun stop() {
        alive = false
        withContext(Dispatchers.IO) {
            synchronized(this) {
                listener?.apply { listener = null }
            }?.close()
        }
    }

    override fun close() {
        CoroutineScope(Dispatchers.IO).launch { stop() }
    }

}
