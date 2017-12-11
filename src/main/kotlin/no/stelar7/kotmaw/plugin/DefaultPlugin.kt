package no.stelar7.kotmaw.plugin

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.annotation.limiter.RateLimiter
import no.stelar7.kotmaw.annotation.producer.Producer
import no.stelar7.kotmaw.annotation.producer.ProductionData
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform
import no.stelar7.kotmaw.util.JsonUtil
import no.stelar7.kotmaw.util.notNull
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private var _producers: HashMap<KClass<*>, MutableList<ProductionData>>? = null
val producers: HashMap<KClass<*>, MutableList<ProductionData>>
    get()
    {
        if (_producers == null)
        {
            _producers = HashMap()
        }
        return _producers ?: throw AssertionError("Set to null by another thread")
    }

private var _limiters: HashMap<Platform, HashMap<APIEndpoint, MutableList<RateLimiter>>>? = null
val limiters: HashMap<Platform, HashMap<APIEndpoint, MutableList<RateLimiter>>>
    get()
    {
        if (_limiters == null)
        {
            _limiters = HashMap()
        }
        return _limiters ?: throw AssertionError("Set to null by another thread")
    }


fun registerRatelimiterType(clazz: KClass<out RateLimiter>)
{
    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registering limiter: ${clazz.simpleName}")
    Platform.values().forEach { platform ->
        val endpointLimits = limiters.getOrPut(platform, { hashMapOf() })

        APIEndpoint.values().forEach { apiEndpoint ->
            val limits = endpointLimits.getOrPut(apiEndpoint, { mutableListOf() })
            limits.add(clazz.createInstance())
        }
    }
}


fun registerProducer(clazz: KClass<*>)
{
    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registering producer: ${clazz.simpleName}")

    clazz.members.forEach { method ->
        val producerAnnotation = method.annotations.find { annotation -> annotation is Producer } as? Producer
        producerAnnotation?.notNull {
            val data = ProductionData(it.priority, it.endpoint, clazz.createInstance(), method, it.limited)
            producers.getOrPut(it.value, { mutableListOf() }).add(data)
            KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registered production of: \"${it.value.simpleName}\" from method: \"${data.endpoint}\" with priority: \"${data.priority}\"")
        }
    }
}


fun sortProducers()
{
    producers.forEach { _, v -> v.sortByDescending { it.priority } }
}

internal inline suspend fun <reified T: Any> get(endpoint: APIEndpoint, data: Map<String, Any>): T
{
    val classMethodPairList = producers[T::class] ?: throw IllegalArgumentException("No producer registered for \"${T::class.simpleName}\"")
    val productionData = classMethodPairList.firstOrNull { it.endpoint == endpoint } ?: throw IllegalArgumentException("No producer registered with method \"$endpoint\"")

    require(data.containsKey("platform"))

    if (productionData.limited)
    {
        runBlocking {
            limiters[data["platform"]]!![endpoint]!!.forEach {
                async {
                    it.getToken()
                }
            }
        }
    }

    val result = productionData.method.call(productionData.instance, data) as HttpResponse

    limiters[data["platform"]]?.get(endpoint)?.forEach {
        it.update(result)
    }


    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${productionData.endpoint}\" from plugin \"${productionData.instance::class.simpleName}\" with priority \"${productionData.priority}\"")
    return JsonUtil.fromJson(result.toString) as T
}
