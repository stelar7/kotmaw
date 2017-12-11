package no.stelar7.kotmaw.producer

import no.stelar7.kotmaw.annotation.producer.Producer
import no.stelar7.kotmaw.dto.Summoner
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.riotconstant.APIEndpoint

class SummonerProducer
{
    var client = HttpClient()

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_NAME, priority = 1, limited = true)
    fun byName(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("name"))

        val platform = data["platform"]
        val name = data["name"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-name/$name")
    }

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_ID, priority = 1, limited = true)
    fun bySummonerId(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/$id")
    }

    @Producer(value = Summoner::class, endpoint = APIEndpoint.SUMMONER_BY_ACCOUNT, priority = 1, limited = true)
    fun byAccountId(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-account/$id")
    }
}