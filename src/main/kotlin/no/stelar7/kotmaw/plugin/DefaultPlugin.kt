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

internal inline suspend fun <reified T: Any> doCommonStuff(endpoint: APIEndpoint, data: Map<String, Any>): List<ProductionData>
{
    val classMethodPairList = producers[T::class] ?: throw IllegalArgumentException("No producer registered for \"${T::class.simpleName}\"")
    val productionData = classMethodPairList.filter { it.endpoint == endpoint }

    require(data.containsKey("platform"))

    if (productionData.first().limited)
    {
        runBlocking {
            limiters[data["platform"]]!![endpoint]!!.forEach {
                async {
                    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Getting token for limiter $it")
                    it.getToken()
                }
            }
        }
    }

    return productionData
}

fun applyLimiting(endpoint: APIEndpoint, data: Map<String, Any>, productionData: List<ProductionData>): HttpResponse
{
    var output: HttpResponse? = null

    productionData.forEach {
        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${it.endpoint}\" from plugin \"${it.instance::class.simpleName}\" with priority \"${it.priority}\"")
        val result = it.method.call(it.instance, data) as HttpResponse

        when (result.responseCode)
        {
            200 -> output = result
            404 -> if (data["404"] as Boolean) output = result

            401 -> throw IllegalArgumentException("API key is missing from call")
            403 -> throw IllegalArgumentException("API key is invalid")
        }

        if (output != null)
        {
            return@forEach
        }

        KotMaw.debugLevel.printIf(DebugLevel.ALL, "\"${it.endpoint}\" from plugin \"${it.instance::class.simpleName}\" with priority \"${it.priority}\" did not produce a valid result")
    }

    if (output == null)
    {
        throw IllegalArgumentException("All producers failed to create a valid response")
    }

    limiters[data["platform"]]!![endpoint]!!.forEach {
        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Updating ratelimits for limiter $it")
        it.update(output!!)
    }

    return output!!
}

internal inline suspend fun <reified T: Any> get(endpoint: APIEndpoint, data: Map<String, Any>): T
{
    val productionData = doCommonStuff<T>(endpoint, data)
    val response = applyLimiting(endpoint, data, productionData)

    return JsonUtil.fromJson(response.toString)
}


internal inline suspend fun <reified T: Any> getMany(endpoint: APIEndpoint, data: Map<String, Any>): List<T>
{
    val productionData = doCommonStuff<T>(endpoint, data)
    val response = applyLimiting(endpoint, data, productionData)

    val list: MutableList<T> = JsonUtil.fromJson(response.toString)
    val resultList: MutableList<T> = mutableListOf()

    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Transforming from List<LinkedTreeMap> to List<T>")

    list.forEach {
        val fixed = JsonUtil.fromJson<T>(JsonUtil.toJson(it))
        resultList.add(fixed)
    }

    return resultList
}