package no.stelar7.kotmaw.util

import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.dto.Summoner
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform

fun KotMaw.summonerByName(platform: Platform.Service, name: String) = async {
    get(APIEndpoint.SUMMONER_BY_NAME, hashMapOf("platform" to platform, "name" to name)) as Summoner
}

fun KotMaw.summonerBySummonerId(platform: Platform.Service, id: Long) = async {
    get(APIEndpoint.SUMMONER_BY_ID, hashMapOf("platform" to platform, "id" to id)) as Summoner
}

fun KotMaw.summonerByAccountId(platform: Platform.Service, id: Long) = async {
    get(APIEndpoint.SUMMONER_BY_ACCOUNT, hashMapOf("platform" to platform, "id" to id)) as Summoner
}
