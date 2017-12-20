import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.debug.DebugLevel


fun main(args: Array<String>)
{
    KotMaw.debugLevel = DebugLevel.NONE
    val api = KotMaw("RGAPI-a899b3e1-6d2b-432c-bd7b-519621765c94")

    runBlocking {
        // run in background (blocking)

        //  api.championMasteries(Platform.Service.EUW1, 19613950).await()
        //  api.summonerByAccountId(Platform.Service.EUW1, 22401330).await()
        //  api.summonerBySummonerId(Platform.Service.EUW1, 19613950).await()
        //  api.summonerByName(Platform.Service.EUW1, "stelar7").await()
        //  api.matchlist(Platform.Service.EUW1, 22401330).await()
        //  api.match(Platform.Service.EUW1, 3452333365).await()
        //  api.timeline(Platform.Service.EUW1, 3452333365).await()
        //  api.thirdPartyCode(Platform.Service.EUW1, 19613950, "Galio").await()
        //  api.challengerLeauge(Platform.Service.EUW1, "RANKED_SOLO_5x5").await()
        //  api.masterLeauge(Platform.Service.EUW1, "RANKED_SOLO_5x5").await()
        //  api.leagueBySummoner(Platform.Service.EUW1, 19613950).await()
        //  api.leagueById(Platform.Service.EUW1, "ad217c20-f84a-11e6-b340-c81f66dd2a8f").await()
        //  api.featuredGames(Platform.Service.EUW1).await()
        //  api.currentGame(Platform.Service.EUW1, 19613950).await()
    }
}
