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

val limiters: HashMap<Platform.Service, HashMap<Enum<*>, MutableList<RateLimiter>>> by lazy {
    HashMap<Platform.Service, HashMap<Enum<*>, MutableList<RateLimiter>>>()
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

        Platform.Service.values().forEach { apiEndpoint ->
            val limits = endpointLimits.getOrPut(apiEndpoint, { mutableListOf() })
            limits.add(clazz.createInstance())
        }
    }
}


fun registerProducer(clazz: KClass<*>, priority: Int? = null)
{
    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registering producer: ${clazz.simpleName}")

    clazz.members.forEach { method ->
        val producerAnnotation = method.annotations.find { annotation -> annotation is Producer } as? Producer
        producerAnnotation?.let {
            val data = ProductionData(priority ?: it.priority, it.endpoint, clazz.createInstance(), method, it.limited)
            producers.getOrPut(it.value, { mutableListOf() }).add(data)
            KotMaw.debugLevel.printIf(DebugLevel.ALL, "Producer of: \"${it.value.simpleName}\" from method: \"${data.endpoint}\" with priority: \"${data.priority}\" added")
        }
    }
}


fun sortProducers()
{
    producers.forEach { _, v -> v.sortByDescending { it.priority } }
}

internal inline suspend fun <reified T: Any> doCommonStuff(endpoint: APIEndpoint): List<ProductionData>
{
    val classMethodPairList = producers[T::class] ?: throw IllegalArgumentException("No producer registered for \"${T::class.simpleName}\"")
    return classMethodPairList.filter { it.endpoint == endpoint }
}

fun applyLimiting(endpoint: APIEndpoint, data: Map<String, Any?>, productionData: List<ProductionData>): HttpResponse
{
    require(data.containsKey("platform"))

    var limit = true
    var response: HttpResponse? = null
    productionData.forEach {
        // Only limit once, and only if that call is limited
        if (it.limited && !limit)
        {
            runBlocking {
                limiters[data["platform"]]!![data["platform"]]!!.plus(limiters[data["platform"]]!![endpoint]!!).forEach {
                    async {
                        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Getting token for limiter $it")
                        it.getToken()
                        limit = true
                    }
                }
            }
        }


        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${it.endpoint}\" from plugin \"${it.instance::class.simpleName}\" with priority \"${it.priority}\"")
        val result = it.method.call(it.instance, data) as HttpResponse
        response = handleResponse(result, data)

        if (response != null)
        {
            return@forEach
        }

        KotMaw.debugLevel.printIf(DebugLevel.ALL, "\"${it.endpoint}\": plugin \"${it.instance::class.simpleName}\": priority \"${it.priority}\" : no valid result")
    }

    if (response == null)
    {
        throw IllegalArgumentException("All producers failed to create a valid response")
    }

    limiters[data["platform"]]!![endpoint]!!.forEach {
        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Updating ratelimits for method limiter $it")
        it.updateLimits(data["platform"] as Platform.Service, endpoint, response!!)
    }

    limiters[data["platform"]]!![data["platform"]]!!.forEach {
        KotMaw.debugLevel.printIf(DebugLevel.ALL, "Updating ratelimits for app limiter $it")
        it.updateLimits(data["platform"] as Platform.Service, data["platform"], response!!)
    }

    return response!!
}

private fun handleResponse(result: HttpResponse, data: Map<String, Any?>): HttpResponse?
{
    return when (result.responseCode)
    {
        200  -> result
        404  -> if (data.containsKey("404")) result else null

        401  -> throw IllegalArgumentException("API key is missing from call")
        403  -> throw IllegalArgumentException("API key is invalid")
        429  -> throw IllegalArgumentException("Ratelimit exceeded")

        else -> throw IllegalArgumentException("Unhandled response code")
    }
}

internal inline suspend fun <reified T: Any> get(endpoint: APIEndpoint, data: Map<String, Any?>): T
{
    return getNullable(endpoint, data)!!
}

internal inline suspend fun <reified T: Any> getNullable(endpoint: APIEndpoint, data: Map<String, Any?>): T?
{
    val productionData = doCommonStuff<T>(endpoint)
    val response = applyLimiting(endpoint, data, productionData)

    return JsonUtil.fromJson(response.toString)
}


internal inline suspend fun <reified T: Any> getMany(endpoint: APIEndpoint, data: Map<String, Any?>): List<T>
{
    val list: List<T> = get(endpoint, data)
    val resultList: MutableList<T> = mutableListOf()

    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Transforming from List<LinkedTreeMap> to List<T>")

    list.forEach {
        val fixed = JsonUtil.fromJson<T>(JsonUtil.toJson(it))!!
        resultList.add(fixed)
    }

    return resultList
}