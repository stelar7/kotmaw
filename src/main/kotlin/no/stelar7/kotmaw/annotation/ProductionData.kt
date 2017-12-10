package no.stelar7.kotmaw.annotation

import kotlin.reflect.KCallable

data class ProductionData(val priority: Int, val name: String, val instance: Any, val method: KCallable<*>)
