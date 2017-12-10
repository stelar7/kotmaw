package no.stelar7.kotmaw.http

data class HttpRequest(val url: String, val method: HttpMethod, val data: Map<String, String>, val headers: Map<String, List<String>>)