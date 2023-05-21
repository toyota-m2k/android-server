package io.github.toyota32k.server.response

enum class StatusCode(val code:Int) {
    Ok(200),
    Created(201),
    Accepted(202),
    PartialContent(206),
    MovedPermanently( 301),
    Found(302),
    NotModified(304),
    BadRequest(400),
    Forbidden(403),
    NotFound(404),
    MethodNotAllowed(405),
    InternalServerError(500),
    ServiceUnavailable(503),
}