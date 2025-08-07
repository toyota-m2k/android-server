package io.github.toyota32k.server.response

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

open class TextHttpResponse(statusCode: StatusCode, contentType:String, private val content:String, onCompleted:((Boolean)->Unit)?=null) : AbstractHttpResponse(statusCode, onCompleted) {
    constructor(statusCode: StatusCode, json:JSONObject) : this(statusCode, CT_JSON, json.toString())
    constructor(statusCode: StatusCode, json:JSONArray) : this(statusCode, CT_JSON, json.toString())

    companion object {
        const val CT_TEXT_PLAIN = "text/plain"
        const val CT_TEXT_HTML = "text/html"
        const val CT_JSON = "application/json"

        fun textPlain(content:String, statusCode:StatusCode=StatusCode.Ok) = TextHttpResponse(statusCode, CT_TEXT_PLAIN, content)
        fun textHtml(content:String, statusCode: StatusCode=StatusCode.Ok) = TextHttpResponse(statusCode, CT_TEXT_HTML, content)
        fun json(content:JSONObject, statusCode: StatusCode=StatusCode.Ok) = TextHttpResponse(statusCode, content)
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