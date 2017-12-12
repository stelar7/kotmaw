package no.stelar7.kotmaw.producer

import no.stelar7.kotmaw.riotconstant.APIEndpoint
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION) annotation class Producer(val value: KClass<*>, val endpoint: APIEndpoint, val priority: Int = 1, val limited: Boolean = true)