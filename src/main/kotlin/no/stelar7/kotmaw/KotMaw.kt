package no.stelar7.kotmaw

import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.annotation.limiter.StandardBurstLimiter
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.plugin.registerProducer
import no.stelar7.kotmaw.plugin.registerRatelimiterType
import no.stelar7.kotmaw.plugin.sortProducers
import no.stelar7.kotmaw.producer.SummonerProducer
import no.stelar7.kotmaw.riotconstant.Platform
import no.stelar7.kotmaw.util.summonerByAccountId
import no.stelar7.kotmaw.util.summonerByName
import no.stelar7.kotmaw.util.summonerBySummonerId

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
        sortProducers()

        registerRatelimiterType(StandardBurstLimiter::class)
    }
}


fun main(args: Array<String>)
{
    val api = KotMaw("RGAPI-0e01902c-3303-4f26-bdda-ae55c8f8324c")

    runBlocking {

        // run in background (blocking)
        api.summonerByAccountId(Platform.Service.EUW1, 22401330).await()
        api.summonerBySummonerId(Platform.Service.EUW1, 19613950).await()
        api.summonerByName(Platform.Service.EUW1, "stelar7").await()
    }

    println("weewoo")

}
