package io.github.toyota32k.server.response

import org.json.JSONObject
import java.io.OutputStream

open class TextHttpResponse(statusCode: StatusCode, contentType:String, private val content:String) : AbstractHttpResponse(statusCode) {
    constructor(statusCode: StatusCode, json:JSONObject) : this(statusCode, CT_JSON, json.toString())

    companion object {
        const val CT_TEXT_PLAIN = "text/plain"
        const val CT_TEXT_HTML = "text/html"
        const val CT_JSON = "application/json"
    }
    init {
        this.contentType = contentType
    }
    lateinit var buffer:ByteArray

    override fun prepare() {
        buffer = content.toByteArray(Charsets.UTF_8)
        contentLength = buffer.size.toLong()
    }

    override fun writeBody(outputStream: OutputStream) {
        outputStream.write(buffer)
    }
}