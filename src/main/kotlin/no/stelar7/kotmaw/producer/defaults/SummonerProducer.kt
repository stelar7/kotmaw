package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

class SummonerProducer
{
    var client = HttpClient()

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_NAME)
    fun byName(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("name"))

        val platform = data["platform"]
        val name = data["name"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-name/$name")
    }

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_ID)
    fun bySummonerId(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/$id")
    }

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_ACCOUNT)
    fun byAccountId(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-account/$id")
    }
}


fun KotMaw.summonerByName(platform: Platform.Service, name: String) = async {
    get<Summoner>(APIEndpoint.SUMMONER_BY_NAME, hashMapOf("platform" to platform, "name" to name))
}

fun KotMaw.summonerBySummonerId(platform: Platform.Service, id: Long) = async {
    get<Summoner>(APIEndpoint.SUMMONER_BY_ID, hashMapOf("platform" to platform, "id" to id))
}

fun KotMaw.summonerByAccountId(platform: Platform.Service, id: Long) = async {
    get<Summoner>(APIEndpoint.SUMMONER_BY_ACCOUNT, hashMapOf("platform" to platform, "id" to id))
}

data class Summoner(val id: Long, val accountId: Long, val name: String, val profileIconId: Int, val revisionDate: Long, val summonerLevel: Int)
