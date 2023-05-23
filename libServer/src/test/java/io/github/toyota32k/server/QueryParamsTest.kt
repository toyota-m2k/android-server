package io.github.toyota32k.server

import junit.framework.TestCase.assertEquals
import org.junit.Test

class QueryParamsTest {
    @Test
    fun queryTest() {
        val url = "https://www.google.com/search?q=kotlin+regex+findall&rlz=1C1TKQJ_jaJP1027JP1027&oq=kotlin+regex+find&aqs=chrome.0.0i512j69i57j0i30l5j0i8i30.3127j0j7&sourceid=chrome&ie=UTF-8"
        val query = QueryParams.parse(url)
        assertEquals("kotlin+regex+findall", query["q"])
        assertEquals("UTF-8", query["ie"])
    }
}