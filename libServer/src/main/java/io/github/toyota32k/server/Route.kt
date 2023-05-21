package io.github.toyota32k.server

import io.github.toyota32k.server.response.IHttpResponse

class Route(
    val name: String, // for debug
    val method: String,
    val regex: Regex,
    val process:(route: Route, request: HttpRequest)-> IHttpResponse
) {
    fun match(url:String):Boolean {
        return regex.matches(url)
    }

    fun execute(request: HttpRequest): IHttpResponse {
        return process(this, request)
    }
}
