package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.dto.ChampionMastery
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.getMany
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

class ChampionMasteryProducer
{

    var client = HttpClient()


    @Producer(value = ChampionMastery::class, endpoint = APIEndpoint.CHAMPION_MASTERY_ALL)
    fun byId(data: Map<String, Any>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]

        return client.makeApiGetRequest("https://$platform.api.riotgames.com/lol/champion-mastery/v3/champion-masteries/by-summoner/$id")
    }
}


fun KotMaw.championMasteries(platform: Platform.Service, id: Long) = async {
    getMany<ChampionMastery>(APIEndpoint.CHAMPION_MASTERY_ALL, hashMapOf("platform" to platform, "id" to id))
}