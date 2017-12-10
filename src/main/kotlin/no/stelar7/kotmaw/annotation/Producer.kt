package no.stelar7.kotmaw.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION) annotation class Producer(val value: KClass<*>, val name: String, val priority: Int)