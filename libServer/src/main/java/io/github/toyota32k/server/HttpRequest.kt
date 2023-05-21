package io.github.toyota32k.server

data class HttpRequest(
    val method:String,
    val url:String,
    val headers:Map<String,String>,
    val content:ByteArray?
) {
    fun contentAsString():String {
        return if(content==null) "" else String(content, Charsets.US_ASCII)
    }
}
