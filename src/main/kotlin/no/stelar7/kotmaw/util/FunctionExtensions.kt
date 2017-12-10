package no.stelar7.kotmaw.util

import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.annotation.Producer
import no.stelar7.kotmaw.annotation.ProductionData
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.riotconstant.Platform
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

inline fun <T: Any> T?.notNull(f: (it: T) -> Unit)
{
    if (this != null)
    {
        f(this)
    }
}

fun KotMaw.registerProducer(clazz: KClass<*>)
{
    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registering producer: ${clazz.simpleName}")

    clazz.members.forEach { method ->
        val producerAnnotation = method.annotations.find { annotation -> annotation is Producer } as? Producer
        producerAnnotation?.notNull {
            val data = ProductionData(it.priority, it.name, clazz.createInstance(), method)
            this.producers.getOrPut(it.value, { mutableListOf() }).add(data)
            KotMaw.debugLevel.printIf(DebugLevel.ALL, "Registered method: ${data.name} with priority: ${data.priority}")
        }
    }
}

internal inline suspend fun <reified T> KotMaw.get(name: String, platform: Platform, id: Any): T
{
    val classMethodPairList = this.producers[T::class] ?: throw IllegalArgumentException("No producer registered for \"${T::class.simpleName}\"")
    val productionData = classMethodPairList.firstOrNull { name.isNotBlank() && it.name.contains(name) } ?: throw IllegalArgumentException("No producer registered with method \"$name\"")
    val result = productionData.method.call(productionData.instance, platform, id) as T

    KotMaw.debugLevel.printIf(DebugLevel.ALL, "Calling method \"${productionData.name}\" from plugin \"${productionData.instance::class.simpleName}\" with priority \"${productionData.priority}\"")
    return result
}