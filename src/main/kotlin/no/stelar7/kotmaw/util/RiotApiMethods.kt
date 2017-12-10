package no.stelar7.kotmaw.util

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.dto.Summoner
import no.stelar7.kotmaw.riotconstant.Platform

fun KotMaw.summonerByName(platform: Platform, name: String): Deferred<Summoner>
{
    return async {
        get("byName", platform, name) as Summoner
    }
}

fun KotMaw.summonerBySummonerId(platform: Platform, id: Long): Deferred<Summoner>
{
    return async {
        get("bySummonerId", platform, id) as Summoner
    }
}

fun KotMaw.summonerByAccountId(platform: Platform, id: Long): Deferred<Summoner>
{
    return async {
        get("byAccountId", platform, id) as Summoner
    }
}