package no.stelar7.kotmaw.dto

data class ChampionMastery(val championLevel: Int,
                           val chestGranted: Boolean,
                           val championPoints: Int,
                           val championPointsSinceLastLevel: Long = 0,
                           val championPointsUntilNextLevel: Long = 0,
                           val tokensEarned: Int = 0,
                           val championId: Int = 0,
                           val lastPlayTime: Long = 0)

