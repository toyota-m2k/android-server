package io.github.toyota32k.server.response

import java.io.File
import java.io.OutputStream
import java.lang.Math.max
import java.lang.Math.min

class StreamingHttpResponse(statusCode: StatusCode, contentType:String, file: File, var start:Long, var end:Long) : FileHttpResponse(statusCode, contentType, file) {
    companion object {
        const val H_ACCEPT_RANGE = "Accept-Range"
        const val H_CONTENT_RANGE = "Content-Range"
    }

    override fun prepare() {
        if (start == 0L && end == 0L) {
            statusCode = StatusCode.Ok
            headers[H_ACCEPT_RANGE] = "bytes"
            super.prepare()
        }
        else {
            val fileLength = file.length();
            if (end == 0L) {
                end = fileLength - 1
            }
            statusCode = StatusCode.PartialContent
            headers[H_CONTENT_RANGE] = "bytes $start-$end/$fileLength"
            headers[H_ACCEPT_RANGE] = "bytes"
            contentLength = end - start + 1
        }
    }

    override fun writeBody(outputStream: OutputStream) {
        if (start == 0L && end == 0L) {
            super.writeBody(outputStream)
        }
        else {
            val chunkLength = (end - start + 1).toInt()
            var remain = chunkLength
            file.inputStream().use { inputStream->
                inputStream.skip(start)
                val buffer = ByteArray(max(chunkLength, 1 * 1024))
                while (remain > 0) {
                    val read = inputStream.read(buffer, 0, min(buffer.size, remain))
                    outputStream.write(buffer, 0, read)
                    remain -= read
                }
            }
        }
    }
}