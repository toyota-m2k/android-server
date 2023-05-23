package io.github.toyota32k.server

object QueryParams {
    private val queryRegex = Regex("[?&](?<name>[^=&]+)(?:=(?<value>[^&=\r\n \t]+))?")
    fun parse(url:String):Map<String,String> {
        return queryRegex.findAll(url).fold(mutableMapOf<String,String>()) { map, m->
            val name = m.groups["name"]?.value ?: return@fold map
            val value = m.groups["value"]?.value
            map[name] = value ?: "true"
            map
        }
    }
}