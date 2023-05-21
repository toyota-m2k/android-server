package io.github.toyota32k.server

import java.net.Socket

interface IClientHandler {
    fun handleClient(s:Socket)
}