package no.stelar7.kotmaw.limiter

import no.stelar7.kotmaw.http.HttpResponse
import java.time.Duration

abstract class RateLimiter(val limits: MutableList<RateLimit>)
{
    abstract fun getToken()
    abstract fun update(data: HttpResponse)

    fun setLimits(limits: MutableList<RateLimit>)
    {
        this.limits.clear()
        this.limits.addAll(limits)
    }
}


data class RateLimit(val limit: Long, val time: Duration)