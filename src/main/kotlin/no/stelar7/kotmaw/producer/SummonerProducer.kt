package no.stelar7.kotmaw.producer

import no.stelar7.kotmaw.annotation.Producer
import no.stelar7.kotmaw.dto.Summoner
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.riotconstant.Platform
import no.stelar7.kotmaw.util.JsonUtil

class SummonerProducer
{
    var client = HttpClient()

    /**
     *
     */
    @Producer(value = Summoner::class, name = "byName", priority = 1)
    fun byName(platform: Platform, name: String): Summoner
    {
        val response = client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-name/$name")
        return JsonUtil.fromJson(response.toString)!!
    }

    @Producer(value = Summoner::class, name = "bySummonerId", priority = 1)
    fun bySummonerId(platform: Platform, id: Long): Summoner
    {
        val response = client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/$id")
        return JsonUtil.fromJson(response.toString)!!
    }

    @Producer(value = Summoner::class, name = "byAccountId", priority = 1)
    fun byAccountId(platform: Platform, id: Long): Summoner
    {
        val response = client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/summoner/v3/summoners/by-account/$id")
        return JsonUtil.fromJson(response.toString)!!
    }
}