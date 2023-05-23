package io.github.toyota32k.server

import io.github.toyota32k.server.response.IHttpResponse
import io.github.toyota32k.server.response.StatusCode
import io.github.toyota32k.server.response.TextHttpResponse

class HttpErrorResponse {
    companion object {
        fun internalServerError(): IHttpResponse {
            return TextHttpResponse(StatusCode.InternalServerError, TextHttpResponse.CT_TEXT_PLAIN, "Internal Server Error.")
        }

        fun notFound(): IHttpResponse {
            return TextHttpResponse(StatusCode.NotFound, TextHttpResponse.CT_TEXT_PLAIN, "Not Found.")
        }

        fun methodNotAllowed(): IHttpResponse {
            return TextHttpResponse(StatusCode.MethodNotAllowed, TextHttpResponse.CT_TEXT_PLAIN, "Method Not Allowed.")
        }

        fun badRequest(msg:String?=null): IHttpResponse {
            return TextHttpResponse(StatusCode.BadRequest, TextHttpResponse.CT_TEXT_PLAIN, msg ?: "Bad Request.")
        }
    }

}
