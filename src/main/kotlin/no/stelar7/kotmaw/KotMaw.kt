package no.stelar7.kotmaw

import no.stelar7.kotmaw.annotation.ProductionData
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.producer.SummonerProducer
import no.stelar7.kotmaw.util.registerProducer
import kotlin.reflect.KClass

class KotMaw(val api_key: String)
{
    companion object
    {
        var debugLevel: DebugLevel = DebugLevel.ALL
        var apiKey: String = "";
    }

    val producers = HashMap<KClass<*>, MutableList<ProductionData>>()

    init
    {
        apiKey = api_key

        registerProducer(SummonerProducer::class)


        producers.forEach { _, v -> v.sortByDescending { it.priority } }
    }
}

/*
fun main(args: Array<String>)
{
    val api = KotMaw()


    launch {
        // run in background (non-blocking)
        api.summonerByAccountId(Platform.EUW1, 22401330).await()
    }

    println("weewoo")

    runBlocking {
        // run in background (blocking)
        api.summonerByAccountId(Platform.EUW1, 22401330).await()
    }

    println("weewoo")
}

*/