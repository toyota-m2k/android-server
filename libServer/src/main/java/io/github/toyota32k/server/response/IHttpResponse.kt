package io.github.toyota32k.server.response

import java.io.OutputStream

interface IHttpResponse {
    companion object {
        const val CORS_DEFAULT_METHODS = "GET,POST,PUT,DELETE,OPTIONS"
        const val CORS_DEFAULT_HEADERS = "Content-Type,Content-Length,Accept"
    }
    fun allowCors(origin:String="*", methods:String?=CORS_DEFAULT_METHODS, headers:String?=CORS_DEFAULT_HEADERS)
    fun writeResponse(outputStream: OutputStream)
    val onCompleted:((Boolean)->Unit)?
}
