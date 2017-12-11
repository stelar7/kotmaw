package no.stelar7.kotmaw.annotation.limiter

import no.stelar7.kotmaw.http.HttpResponse
import java.time.Duration

class StandardBurstLimiter(limits: MutableList<RateLimit>): RateLimiter(limits)
{
    override fun update(data: HttpResponse)
    {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    constructor(): this(mutableListOf<RateLimit>(RateLimit(10, Duration.ofSeconds(1)), RateLimit(100, Duration.ofSeconds(120))))

    override fun getToken()
    {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}