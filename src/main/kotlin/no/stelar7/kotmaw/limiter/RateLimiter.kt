package no.stelar7.kotmaw.limiter

import no.stelar7.kotmaw.http.HttpResponse
import java.time.Duration

abstract class RateLimiter
{
    abstract fun getToken()
    abstract fun update(data: HttpResponse)
}


data class RateLimit(val limit: Long, val time: Duration)