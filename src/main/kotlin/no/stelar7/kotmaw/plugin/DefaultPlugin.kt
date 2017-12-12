package no.stelar7.kotmaw.plugin

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.limiter.RateLimiter
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.producer.ProductionData
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform
import no.stelar7.kotmaw.util.JsonUtil
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

val producers: HashMap<KClass<*>, MutableList<ProductionData>> by lazy {
    HashMap<KClass<*>, MutableList<ProductionData>>()
}

val limiters: HashMap<Platform.Service, HashMap<APIEndpoint, MutableList<RateLimiter>>> by lazy {
    HashMap<Platform.Service, HashMap<APIEndpoint, MutableList<RateLimiter>>>()
}


fun registerRatelimiterType(clazz: KClass<out RateLimiter>)
{
    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registering limiter: ${clazz.simpleName}")
    Platform.Service.values().forEach { platform ->
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
        producerAnnotation?.let {
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

internal inline suspend fun <reified T: Any> doCommonStuff(endpoint: APIEndpoint, data: Map<String, Any>): ProductionData
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

    return productionData
}

fun applyLimiting(endpoint: APIEndpoint, productionData: ProductionData, data: Map<String, Any>): HttpResponse
{
    val result = productionData.method.call(productionData.instance, data) as HttpResponse

    limiters[data["platform"]]!![endpoint]!!.forEach {
        it.update(result)
    }

    return result
}

internal inline suspend fun <reified T: Any> get(endpoint: APIEndpoint, data: Map<String, Any>): T
{
    val productionData: ProductionData = doCommonStuff<T>(endpoint, data)
    val response: HttpResponse = applyLimiting(endpoint, productionData, data)

    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${productionData.endpoint}\" from plugin \"${productionData.instance::class.simpleName}\" with priority \"${productionData.priority}\"")
    return JsonUtil.fromJson(response.toString) as T
}


internal inline suspend fun <reified T: Any> getMany(endpoint: APIEndpoint, data: Map<String, Any>): List<T>
{
    val productionData: ProductionData = doCommonStuff<T>(endpoint, data)
    val response: HttpResponse = applyLimiting(endpoint, productionData, data)

    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${productionData.endpoint}\" from plugin \"${productionData.instance::class.simpleName}\" with priority \"${productionData.priority}\"")

    val list: MutableList<T> = JsonUtil.fromJson(response.toString)
    val resultList: MutableList<T> = mutableListOf()

    list.forEach {
        val fixed = JsonUtil.fromJson(JsonUtil.toJson(it)) as T
        resultList.add(fixed)
    }

    return resultList
}