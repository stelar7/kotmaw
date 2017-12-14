package no.stelar7.kotmaw.limiter

import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.debug.DebugLevel
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.riotconstant.Platform
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StandardBurstLimiter: RateLimiter()
{
    val limits: MutableList<RateLimit> by lazy {
        mutableListOf<RateLimit>()
    }

    val callCount: HashMap<RateLimit, Long> by lazy {
        HashMap<RateLimit, Long>()
    }

    val firstCall: HashMap<RateLimit, Instant> by lazy {
        HashMap<RateLimit, Instant>()
    }

    var lock = ReentrantLock()


    override fun updateLimits(platform: Platform.Service, endpoint: Any?, output: HttpResponse)
    {
        lock.withLock {

            val lheader = output.responseHeaders[if (platform == endpoint) "X-App-Rate-Limit" else "X-Method-Rate-Limit"]!!
            val cheader = output.responseHeaders[if (platform == endpoint) "X-App-Rate-Limit-Count" else "X-Method-Rate-Limit-Count"]!!
            val bounds: HashMap<Long, Duration> = HashMap()
            val counts: HashMap<Int, Int> = HashMap()

            lheader[0].split(",").forEach {
                val split = it.split(":")
                bounds.put(split[0].toLong(), Duration.ofSeconds(split[1].toLong()))
            }

            cheader[0].split(",").forEach {
                val split = it.split(":")
                counts.put(split[1].toInt(), split[0].toInt())
            }

            bounds.forEach {
                val limiter = RateLimit(it.key, it.value)
                val limiterCount = counts[limiter.time.seconds.toInt()]!!

                if (!limits.contains(limiter))
                {
                    limits.add(limiter)
                    KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "found new limiter $limiter")
                }

                if (!callCount.containsKey(limiter))
                {
                    callCount.put(limiter, limiterCount.toLong())
                } else
                {
                    if (limiterCount > callCount[limiter]!!)
                    {
                        KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "$limiter limit changed from ${callCount[limiter]} to $limiterCount")
                        callCount[limiter] = limiterCount.toLong()
                    }
                }
            }
        }
    }

    @Synchronized override fun getToken()
    {
        lock.withLock {
            updateCalls()

            getSleepTime()?.let {
                KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "$this is sleeping for $it milliseconds")
                Thread.sleep(it)
            }
        }
    }

    private fun updateCalls()
    {
        val now = Instant.now()

        limits.forEach {
            callCount.compute(it, { _, value -> value?.let { it + 1 } ?: 1 })
            firstCall.putIfAbsent(it, now)

            if (firstCall[it]!!.toEpochMilli() + it.time.toMillis() - now.toEpochMilli() < 0)
            {
                firstCall[it] = now
                callCount[it] = 0
                KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "resetting limiter $it")
            }
        }
    }

    private fun getSleepTime(): Long?
    {
        val now = Instant.now()
        var currentDelay: Long? = null

        if (limits.isEmpty())
        {
            KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "No ratelimiter registered yet")
            return currentDelay
        }

        limits.forEach {
            val calls = callCount[it]!!

            if (calls >= it.limit)
            {
                KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "$it is over limit! ($calls >= ${it.limit})")

                val sleep = firstCall[it]!!.toEpochMilli() + it.time.toMillis() - now.toEpochMilli()
                if ((currentDelay ?: 0) < sleep)
                {
                    KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "New sleep time is $sleep milliseconds")
                    currentDelay = sleep
                }
            } else
            {
                KotMaw.debugLevel.printIf(DebugLevel.EXTENDED, "Under ratelimit")
            }
        }
        return currentDelay
    }

    override fun toString(): String
    {
        return "StandardBurstLimiter(limits=$limits)"
    }


}