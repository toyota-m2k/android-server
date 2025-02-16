package io.github.toyota32k.server

import org.json.JSONObject

data class HttpRequest(
    val method:String,
    val url:String,
    val headers:Map<String,String>,
    val content:ByteArray?
) {
    fun contentAsString():String {
        return if(content==null) "" else String(content, Charsets.US_ASCII)
    }
    fun contentAsJson():JSONObject? {
        return try {
            JSONObject(contentAsString())
        } catch (e:Exception) {
            null
        }
    }
}
