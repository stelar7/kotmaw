package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.plugin.getNullable
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

class SpectatorProducer
{
    var client = HttpClient()


    @Producer(value = SpectatorGameInfo::class, endpoint = APIEndpoint.SPECTATOR_CURRENT)
    fun byId(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/spectator/v3/active-games/by-summoner/$id"

        return client.makeApiGetRequest(url)
    }

    @Producer(value = FeaturedGames::class, endpoint = APIEndpoint.SPECTATOR_FEATURED)
    fun featured(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))

        val platform = data["platform"]
        val url = "https://$platform.api.riotgames.com/lol/spectator/v3/featured-games"

        return client.makeApiGetRequest(url)
    }
}


fun KotMaw.currentGame(platform: Platform.Service, id: Long): Deferred<SpectatorGameInfo?> = async {
    getNullable<SpectatorGameInfo>(APIEndpoint.SPECTATOR_CURRENT, hashMapOf("platform" to platform, "id" to id, "404" to 404))
}

fun KotMaw.featuredGames(platform: Platform.Service) = async {
    get<FeaturedGames>(APIEndpoint.SPECTATOR_FEATURED, hashMapOf("platform" to platform))
}

data class FeaturedGames(val clientRefreshInterval: Long, val gameList: List<SpectatorGameInfo>)

data class SpectatorGameInfo(val bannedChampions: List<BannedChampion>,
                             val gameId: Long,
                             val gameLength: Long,
                             val gameMode: String,
                             val gameQueueConfigId: Long,
                             val gameStartTime: Long,
                             val gameType: String,
                             val mapId: Long,
                             val observers: SpectatorObserver,
                             val participants: List<SpectatorParticipant>,
                             val platformId: String)

data class SpectatorParticipant(val championId: Int,
                                val profileIconId: Long,
                                val spell1Id: Long,
                                val spell2Id: Long,
                                val summonerName: String,
                                val teamId: Long,
                                val bot: Boolean,
                                val perks: SpectatorPerks?,
                                val masteries: List<SpectatorMastery>?,
                                val runes: List<SpectatorRune>?,
                                val summonerId: Long)

data class SpectatorObserver(val encryptionKey: String)
data class SpectatorRune(private val runeId: Int, private val count: Int)
data class SpectatorMastery(private val masteryId: Int, private val rank: Int)
data class SpectatorPerks(val perkIds: List<Int>, val perkStyle: Int, val perkSubStyle: Int)
