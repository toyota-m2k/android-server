package io.github.toyota32k.server

import io.github.toyota32k.server.response.IHttpResponse
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Socket


class HttpProcessor(routes:Array<Route>?=null) : IClientHandler {
    companion object {
        val headerRegex = Regex("([a-zA-Z-]+): *(.*)")
        val logger = HttpServer.logger
    }
    private val routes = mutableListOf<Route>()

    init {
        if(routes!=null) {
            addRoutes(routes)
        }
    }

    fun addRoute(route: Route) {
        routes.add(route)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun addRoutes(routes:Array<Route>) {
        routes.forEach { addRoute(it) }
    }

    override fun handleClient(s: Socket) {
        logger.debug()
        s.use {
            BufferedInputStream(s.getInputStream()).use { inputStream ->
            BufferedOutputStream(s.getOutputStream()).use { outputStream ->
                try {
                    val request = getRequest(inputStream)
                    logger.debug("processing: $request")

                    // route and handle the request...
                    val response = routeRequest(request)
                    try {
                        response.writeResponse(outputStream)
                        outputStream.flush()
                        logger.debug("completed: $request")
                        response.onCompleted?.invoke(true)
                    } catch (e:Throwable) {
                        logger.error(e)
                        response.onCompleted?.invoke(false)
                    }
                }
                catch (e:Throwable) {
                    // getRequest() ... routeRequest() で例外が発生した場合
                    logger.error(e)
                    HttpErrorResponse.internalServerError().writeResponse(outputStream)
                    outputStream.flush()
                }
            }}
        }
    }

    private fun readLine(reader: InputStream): String {
        val buffer = ByteArrayOutputStream(1024)
        var nextChar: Int
        while (true) {
            nextChar = reader.read()
            if(nextChar==-1) {
                break
            }
            if (nextChar == '\n'.code) {
                break
            }
            if (nextChar == '\r'.code) {
                continue
            }
            buffer.write(nextChar)
        }
        return String(buffer.toByteArray(), Charsets.US_ASCII)
    }

    private fun getRequest(inputStream: InputStream): HttpRequest {
        //Read Request Line
        val request = readLine(inputStream)

        val tokens = request.split(' ')
        if (tokens.size != 3) {
            throw IllegalArgumentException("invalid http request line")
        }
        val method = tokens[0].uppercase()
        val url = tokens[1]
//        val protocolVersion = tokens[2]

        //Read Headers
        val headers = mutableMapOf<String,String>()
        while (true) {
            val line = readLine(inputStream)
            if (line.isEmpty()) {
                break
            }

            val match = headerRegex.find(line) ?: continue
            if(match.groups.size == 3) {
                val name = match.groups[1]?.value
                val value = match.groups[2]?.value
                if(name!=null && value!=null) {
                    headers[name] = value
                }
            }
        }

        var content:ByteArray? = null
        val contentLength = headers["Content-Length"]?.toIntOrNull()
        if (contentLength!=null) {
            val totalBytes = contentLength
            var bytesLeft = totalBytes
            val bytes = ByteArray(totalBytes)
            content = bytes

            while (bytesLeft > 0) {
                val read = inputStream.read(bytes, totalBytes-bytesLeft, bytesLeft)
                if(read==-1) {
                    content = null
                }
                bytesLeft-=read
            }
        }
        return HttpRequest(method,url,headers,content)
    }

    private fun routeRequest(request: HttpRequest): IHttpResponse {
        val routes = this.routes.filter { it.match(request.url) }

        if (!routes.any()) {
            return HttpErrorResponse.notFound()
        }

        val route = routes.firstOrNull { it.method == request.method }
            ?: return HttpErrorResponse.methodNotAllowed()

        // extract the path if there is one
//        var match = Regex.Match(request.Url, route.UrlRegex);
//        if (match.Groups.Count > 1) {
//            request.Path = match.Groups[1].Value;
//        } else {
//            request.Path = request.Url;
//        }

        // trigger the route handler...
        return try {
            route.execute(request)
        } catch (_:Throwable) {
    //            log.error(ex)
            HttpErrorResponse.internalServerError()
        }
    }
}
