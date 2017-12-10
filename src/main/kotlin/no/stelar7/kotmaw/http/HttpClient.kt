package no.stelar7.kotmaw.http

import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.util.JsonUtil
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient
{

    fun makeGetRequest(url: String): HttpResponse
    {
        return makeRequest(HttpRequest(url, HttpMethod.GET, emptyMap(), emptyMap()))
    }

    fun makePostRequest(url: String, data: Map<String, String>): HttpResponse
    {
        return makeRequest(HttpRequest(url, HttpMethod.POST, data, emptyMap()))
    }

    fun makeApiGetRequest(url: String): HttpResponse
    {
        return makeRequest(HttpRequest(url, HttpMethod.GET, emptyMap(), mapOf("X-Riot-Token" to listOf(KotMaw.apiKey))))
    }

    private fun makeRequest(request: HttpRequest): HttpResponse
    {
        val urlObj = URL(request.url)

        with(urlObj.openConnection() as HttpURLConnection) {
            KotMaw.debugLevel.printIf(DebugLevel.BASIC, "Making request to $url")

            doInput = true
            requestMethod = request.method.name


            request.headers.forEach { key, value ->
                this.setRequestProperty(key, value.joinToString(separator = ","))
                KotMaw.debugLevel.printIf(DebugLevel.ALL, "Added header: $key = $value")
            }

            if (request.method == HttpMethod.POST)
            {
                doOutput = true

                DataOutputStream(outputStream).use {
                    it.writeBytes(JsonUtil.toJson(request.data))
                    KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "Writing post data: ${request.data}")
                }
            }

            BufferedReader(InputStreamReader(inputStream)).use {
                val buffer = StringBuffer()

                var inData = it.readLine()
                do
                {
                    buffer.append(inData)
                    inData = it.readLine()
                } while (inData != null)

                KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "Returned data: $buffer")
                return HttpResponse(responseCode, buffer.toString(), headerFields)
            }
        }
    }
}