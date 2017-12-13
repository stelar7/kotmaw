package no.stelar7.kotmaw.limiter

import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.riotconstant.Platform
import java.time.Duration

abstract class RateLimiter
{
    abstract fun getToken()
    abstract fun updateLimits(platform: Platform.Service, endpoint: Any?, output: HttpResponse)
}


data class RateLimit(val limit: Long, val time: Duration)