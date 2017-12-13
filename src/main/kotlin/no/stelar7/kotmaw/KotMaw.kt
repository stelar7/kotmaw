package no.stelar7.kotmaw

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.limiter.StandardBurstLimiter
import no.stelar7.kotmaw.plugin.registerProducer
import no.stelar7.kotmaw.plugin.registerRatelimiterType
import no.stelar7.kotmaw.plugin.sortProducers
import no.stelar7.kotmaw.producer.defaults.*
import no.stelar7.kotmaw.riotconstant.Platform

class KotMaw(api_key: String)
{
    companion object
    {
        var debugLevel: DebugLevel = DebugLevel.ALL
        var apiKey: String = ""
    }

    init
    {
        apiKey = api_key

        registerProducer(SummonerProducer::class)
        registerProducer(ChampionMasteryProducer::class)
        sortProducers()

        registerRatelimiterType(StandardBurstLimiter::class)
    }
}


fun main(args: Array<String>)
{
    val api = KotMaw("RGAPI-9a66e168-f840-45df-9867-d4d06255e10f")

    runBlocking {
        launch {
            // run in background (blocking)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)
            api.championMasteries(Platform.Service.EUW1, 19613950)


            api.summonerByAccountId(Platform.Service.EUW1, 22401330).await()
            api.summonerBySummonerId(Platform.Service.EUW1, 19613950).await()
            api.summonerByName(Platform.Service.EUW1, "stelar7").await()
        }.join()
    }

    println("weewoo")

}
