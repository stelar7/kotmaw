package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.plugin.getMany
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform


class LeagueProducer
{
    var client = HttpClient()


    @Producer(value = LeagueList::class, endpoint = APIEndpoint.LEAGUE_CHALLENGER)
    fun challenger(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("queue"))

        val platform = data["platform"]
        val queue = data["queue"]
        val url = "https://$platform.api.riotgames.com/lol/league/v3/challengerleagues/by-queue/$queue"

        return client.makeApiGetRequest(url)
    }

    @Producer(value = LeagueList::class, endpoint = APIEndpoint.LEAGUE_MASTER)
    fun master(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("queue"))

        val platform = data["platform"]
        val queue = data["queue"]
        val url = "https://$platform.api.riotgames.com/lol/league/v3/masterleagues/by-queue/$queue"

        return client.makeApiGetRequest(url)
    }

    @Producer(value = LeagueList::class, endpoint = APIEndpoint.LEAGUE_BY_ID)
    fun byId(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/league/v3/leagues/$id"

        return client.makeApiGetRequest(url)
    }

    @Producer(value = LeaguePosition::class, endpoint = APIEndpoint.LEAGUE_BY_SUMMONER)
    fun bySummoner(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/league/v3/positions/by-summoner/$id"

        return client.makeApiGetRequest(url)
    }
}


fun KotMaw.challengerLeauge(platform: Platform.Service, queue: String) = async {
    get<LeagueList>(APIEndpoint.LEAGUE_CHALLENGER, hashMapOf("platform" to platform, "queue" to queue))
}

fun KotMaw.masterLeauge(platform: Platform.Service, queue: String) = async {
    get<LeagueList>(APIEndpoint.LEAGUE_CHALLENGER, hashMapOf("platform" to platform, "queue" to queue))
}

fun KotMaw.leagueById(platform: Platform.Service, id: String) = async {
    get<LeagueList>(APIEndpoint.LEAGUE_BY_ID, hashMapOf("platform" to platform, "id" to id))
}

fun KotMaw.leagueBySummoner(platform: Platform.Service, id: Long) = async {
    getMany<LeaguePosition>(APIEndpoint.LEAGUE_BY_SUMMONER, hashMapOf("platform" to platform, "id" to id))
}


data class LeagueList(val entries: List<LeagueItem>, val name: String, val queue: String, val tier: String, val leagueId: String)

data class LeagueItem(val rank: String,
                      val hotStreak: Boolean,
                      val miniSeries: MiniSeries?,
                      val wins: Int,
                      val veteran: Boolean,
                      val losses: Int,
                      val freshBlood: Boolean,
                      val playerOrTeamName: String,
                      val inactive: Boolean,
                      val playerOrTeamId: Long,
                      val leaguePoints: Int)

data class MiniSeries(private val losses: Int, private val progress: String, private val target: Int, private val wins: Int)

data class LeaguePosition(val rank: String,
                          val queueType: String,
                          val hotStreak: Boolean,
                          val miniSeries: MiniSeries?,
                          val wins: Int,
                          val veteran: Boolean,
                          val losses: Int,
                          val playerOrTeamId: String,
                          val leagueName: String,
                          val playerOrTeamName: String,
                          val inactive: Boolean,
                          val freshBlood: Boolean,
                          val tier: String,
                          val leaguePoints: Int,
                          val leagueId: String)