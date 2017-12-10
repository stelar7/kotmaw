package no.stelar7.kotmaw.http

data class HttpResponse(val responseCode: Int, val toString: String, val responseHeaders: Map<String, List<String>>)

