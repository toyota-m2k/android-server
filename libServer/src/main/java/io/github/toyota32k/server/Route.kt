package io.github.toyota32k.server

import io.github.toyota32k.server.response.IHttpResponse

enum class HttpMethod {
    GET, POST, PUT, DELETE,
}

class Route(
    val name: String, // for debug
    val method: String,
    val regex: Regex,
    val process:(route: Route, request: HttpRequest)-> IHttpResponse
) {
    constructor(name:String, method:String, pattern: String, process:(route: Route, request: HttpRequest)-> IHttpResponse) : this(name, method, Regex(pattern), process)
    constructor(name:String, method:HttpMethod, regex: Regex, process:(route: Route, request: HttpRequest)-> IHttpResponse) : this(name, method.toString(), regex, process)
    constructor(name:String, method:HttpMethod, pattern: String, process:(route: Route, request: HttpRequest)-> IHttpResponse) : this(name, method.toString(), Regex(pattern), process)

    fun match(url:String):Boolean {
        return regex.matches(url)
    }

    fun execute(request: HttpRequest): IHttpResponse {
        return process(this, request)
    }
}
