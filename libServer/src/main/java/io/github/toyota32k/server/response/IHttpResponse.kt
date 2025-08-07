package io.github.toyota32k.server.response

import java.io.OutputStream

interface IHttpResponse {
    fun writeResponse(outputStream: OutputStream)
    val onCompleted:((Boolean)->Unit)?
}
