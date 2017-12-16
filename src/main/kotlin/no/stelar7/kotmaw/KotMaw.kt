package no.stelar7.kotmaw

import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.limiter.StandardBurstLimiter
import no.stelar7.kotmaw.plugin.registerProducer
import no.stelar7.kotmaw.plugin.registerRatelimiterType
import no.stelar7.kotmaw.plugin.sortProducers
import no.stelar7.kotmaw.producer.defaults.ChampionMasteryProducer
import no.stelar7.kotmaw.producer.defaults.MatchProducer
import no.stelar7.kotmaw.producer.defaults.SummonerProducer
import no.stelar7.kotmaw.producer.defaults.ThirdPartyProducer

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
        registerProducer(MatchProducer::class)
        registerProducer(ThirdPartyProducer::class)
        sortProducers()

        registerRatelimiterType(StandardBurstLimiter::class)
    }
}


fun main(args: Array<String>)
{
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
