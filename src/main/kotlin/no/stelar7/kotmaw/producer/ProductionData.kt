package no.stelar7.kotmaw.producer

import no.stelar7.kotmaw.riotconstant.APIEndpoint
import kotlin.reflect.KCallable

data class ProductionData(val priority: Int, val endpoint: APIEndpoint, val instance: Any, val method: KCallable<*>, val limited: Boolean)
