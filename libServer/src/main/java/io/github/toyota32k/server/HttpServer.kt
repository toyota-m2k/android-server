package io.github.toyota32k.server

import io.github.toyota32k.logger.UtLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.net.ssl.SSLContext

/**
 *   SSLを有効にする方法
 *
 *   val keyStore = KeyStore.getInstance("PKCS12").apply {
 *       assets.open("server.p12").use { load(it, "password".toCharArray()) }
 *   }
 *   val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
 *       init(keyStore, "password".toCharArray())
 *   }
 *   val sslContext = SSLContext.getInstance("TLS").apply {
 *       init(kmf.keyManagers, null, null)
 *   }
 *   httpServer.readTimeoutMs = 60000   // 任意で変更
 *   httpServer.start(port, sslContext)
 *
 */

class HttpServer(routes:Array<Route>) : AutoCloseable {
    companion object {
        val logger = UtLog("SVR", null, this::class.java)
    }
    val httpProcessor = HttpProcessor(routes)

    var serverLooper: Looper? = null
        get() = synchronized(this) { field }
        set(v) = synchronized(this) { field = v }

    /**
     * 各接続の read タイムアウト (ミリ秒)。デフォルト 30000 ms。
     * 0 でタイムアウトなし。次の接続から有効。
     */
    var readTimeoutMs: Int
        get() = httpProcessor.readTimeoutMs
        set(v) { httpProcessor.readTimeoutMs = v }

    enum class State {
        INITIAL,
        RUNNING,
        STOPPING,
        STOPPED,
    }
    val serverState = MutableStateFlow(State.INITIAL)

    /**
     * サーバを起動する。
     *
     * @param port       listen するポート
     * @param sslContext HTTPS で listen する場合は、アプリ側で構築済の SSLContext を渡す。
     *                   null なら HTTP で listen する。TLS バージョン制限や鍵管理は
     *                   アプリ側で SSLContext を構築する時に行うこと (ライブラリは関与しない)。
     */
    fun start(port:Int, sslContext: SSLContext? = null) {
        logger.debug()
        if(serverLooper!=null) throw IllegalStateException("http server is already running.")
        val looper = Looper().apply { serverLooper = this }
        CoroutineScope(Dispatchers.IO).launch {
            serverState.value = State.RUNNING
            looper.loop(port, httpProcessor, sslContext)
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