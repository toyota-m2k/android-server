package io.github.toyota32k.server.response

import java.io.OutputStream

abstract class AbstractHttpResponse(
    var statusCode: StatusCode,
    override val onCompleted:((Boolean)->Unit)?
) : IHttpResponse {
    companion object {
        const val H_CONTENT_TYPE = "Content-Type"
        const val H_CONTENT_LENGTH = "Content-Length"

        fun writeText(outputStream: OutputStream, text:String) {
            val bytes = text.toByteArray(Charsets.US_ASCII)
            outputStream.write(bytes, 0, bytes.size)
        }

    }
    val headers = mutableMapOf<String,String>()

    private fun setHeaderValue(key:String,value:String?) {
        if (value != null) {
            headers[key] = value
        } else {
            headers.remove(key)
        }
    }

    var contentType:String?
        get() = headers[H_CONTENT_TYPE]
        set(v) = setHeaderValue(H_CONTENT_TYPE, v)

    var contentLength:Long
        get() =  headers[H_CONTENT_TYPE]?.toLongOrNull() ?: 0L
        set(v) = setHeaderValue(H_CONTENT_LENGTH, if(v<0) null else v.toString())

    protected abstract fun prepare()

    protected fun writeHeaders(outputStream:OutputStream) {
        writeText(outputStream, "HTTP/1.0 ${statusCode.code} ${this.statusCode}\r\n")
        writeText(outputStream, headers.map {"${it.key}: ${it.value}" }.joinToString("\r\n"))
        writeText(outputStream, "\r\n")
    }

    protected abstract fun writeBody(outputStream: OutputStream)

    override fun writeResponse(outputStream: OutputStream) {
        prepare()
        writeHeaders(outputStream)
        writeText(outputStream, "\r\n")
        writeBody(outputStream)
    }
}