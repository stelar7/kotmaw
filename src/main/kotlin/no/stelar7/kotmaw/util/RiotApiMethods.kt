package no.stelar7.kotmaw.util

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.dto.Summoner
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

fun KotMaw.summonerByName(platform: Platform, name: String): Deferred<Summoner>
{
    return async {
        get(APIEndpoint.SUMMONER_BY_NAME, hashMapOf("platform" to platform, "name" to name)) as Summoner
    }
}

fun KotMaw.summonerBySummonerId(platform: Platform, id: Long): Deferred<Summoner>
{
    return async {
        get(APIEndpoint.SUMMONER_BY_ID, hashMapOf("platform" to platform, "id" to id)) as Summoner
    }
}

fun KotMaw.summonerByAccountId(platform: Platform, id: Long): Deferred<Summoner>
{
    return async {
        get(APIEndpoint.SUMMONER_BY_ACCOUNT, hashMapOf("platform" to platform, "id" to id)) as Summoner
    }
}
