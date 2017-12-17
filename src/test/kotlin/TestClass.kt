
import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.debug.DebugLevel


fun main(args: Array<String>)
{
    KotMaw.debugLevel = DebugLevel.ALL
    val api = KotMaw("RGAPI-fd0eb940-46e9-4dfc-921b-3b6b4390da6a")

    runBlocking {
        // run in background (blocking)
        //            api.championMasteries(Platform.Service.EUW1, 19613950)
        //            api.summonerByAccountId(Platform.Service.EUW1, 22401330)
        //            api.summonerBySummonerId(Platform.Service.EUW1, 19613950)
        //            api.summonerByName(Platform.Service.EUW1, "stelar7")
        //            api.matchlist(Platform.Service.EUW1, 22401330).await().matches.forEach { println(it) }
        //            api.match(Platform.Service.EUW1, 3452333365).await()
        //            api.timeline(Platform.Service.EUW1, 3452333365).await()
        //            api.thirdPartyCode(Platform.Service.EUW1, 19613950, "Galio").await()

    }
}
