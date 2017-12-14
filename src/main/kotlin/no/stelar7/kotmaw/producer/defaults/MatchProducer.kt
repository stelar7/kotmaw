package no.stelar7.kotmaw.producer.defaults

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform


class MatchProducer
{

    var client = HttpClient()


    @Producer(value = MatchList::class, endpoint = APIEndpoint.MATCHLIST)
    fun byId(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        var url = "https://$platform.api.riotgames.com/lol/match/v3/matchlists/by-account/$id"

        data.filterKeys { it != "platform" && it != "id" }.filter { it.component2() != null }.asIterable().forEachIndexed { i, (k, v) ->

            val value: Any? = (v as? Set<*>)?.joinToString(separator = ",") ?: v

            url += if (i == 0)
            {
                "?$k=$value"
            } else
            {
                "&$k=$value"
            }
        }


        return client.makeApiGetRequest(url)
    }
}

data class MatchListParams(var queue: Set<Int>? = null,
                           var season: Set<Int>? = null,
                           var champion: Set<Int>? = null,
                           var beginTime: Long? = null,
                           var endTime: Long? = null,
                           var beginIndex: Int? = null,
                           var endIndex: Int? = null)


fun KotMaw.matchlist(platform: Platform.Service, id: Long, params: () -> MatchListParams) = async {
    val map: HashMap<String, Any?> = hashMapOf("queue" to params().queue,
                                               "season" to params().season,
                                               "champion" to params().champion,
                                               "beginTime" to params().beginTime,
                                               "endTime" to params().endTime,
                                               "beginIndex" to params().beginIndex,
                                               "endIndex" to params().endIndex,
                                               "platform" to platform,
                                               "id" to id)

    get<MatchList>(APIEndpoint.MATCHLIST, map)
}

data class MatchList(val matches: List<MatchReference>, val totalGames: Int, val startIndex: Int, val endIndex: Int)

data class MatchReference(val lane: String,
                          val gameId: Long,
                          val champion: Int,
                          val platformId: String,
                          val season: Int,
                          val queue: Int,
                          val role: String,
                          val timestamp: Long)