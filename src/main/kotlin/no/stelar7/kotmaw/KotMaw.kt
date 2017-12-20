package no.stelar7.kotmaw

import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.limiter.StandardBurstLimiter
import no.stelar7.kotmaw.plugin.registerProducer
import no.stelar7.kotmaw.plugin.registerRatelimiterType
import no.stelar7.kotmaw.plugin.sortProducers
import no.stelar7.kotmaw.producer.defaults.*

class KotMaw(api_key: String)
{
    companion object
    {
        var debugLevel: DebugLevel = DebugLevel.NONE
        var apiKey: String = ""
    }

    init
    {
        apiKey = api_key

        registerProducer(SummonerProducer::class)
        registerProducer(ChampionMasteryProducer::class)
        registerProducer(MatchProducer::class)
        registerProducer(ThirdPartyProducer::class)
        registerProducer(LeagueProducer::class)
        sortProducers()

        registerRatelimiterType(StandardBurstLimiter::class)
    }
}
