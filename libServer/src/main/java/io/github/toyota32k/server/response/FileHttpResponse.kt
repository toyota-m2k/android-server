package io.github.toyota32k.server.response

import java.io.File
import java.io.OutputStream

open class FileHttpResponse(statusCode: StatusCode, contentType:String, val file: File, onCompleted:((Boolean)->Unit)?=null) : AbstractHttpResponse(statusCode, onCompleted) {
    init {
        this.contentType = contentType
    }
    override fun prepare() {
        contentLength = file.length()
    }

    override fun writeBody(outputStream: OutputStream) {
        file.inputStream().use { inputStream->
            inputStream.copyTo(outputStream)
            outputStream.flush()
        }
    }
}