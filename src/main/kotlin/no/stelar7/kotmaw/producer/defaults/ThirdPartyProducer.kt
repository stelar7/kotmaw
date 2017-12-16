package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

class ThirdPartyProducer
{
    var client = HttpClient()

    @Producer(value = ThirdPartyCode::class, endpoint = APIEndpoint.THIRD_PARTY_CODE)
    fun match(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/platform/v3/third-party-code/by-summoner/$id"

        return client.makeApiGetRequest(url)
    }
}

fun KotMaw.thirdPartyCode(platform: Platform.Service, id: Long, compareTo: String) = async {
    val code = get<ThirdPartyCode>(APIEndpoint.THIRD_PARTY_CODE, hashMapOf("platform" to platform, "id" to id))

    code.value == compareTo
}


class ThirdPartyCode(val value: String)
