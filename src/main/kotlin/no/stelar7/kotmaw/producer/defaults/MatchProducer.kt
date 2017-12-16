package no.stelar7.kotmaw.producer.defaults

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.experimental.async
import no.stelar7.kotmaw.KotMaw
import no.stelar7.kotmaw.http.HttpClient
import no.stelar7.kotmaw.http.HttpResponse
import no.stelar7.kotmaw.plugin.get
import no.stelar7.kotmaw.producer.Producer
import no.stelar7.kotmaw.riotconstant.APIEndpoint
import no.stelar7.kotmaw.riotconstant.Platform
import no.stelar7.kotmaw.util.JsonUtil


class MatchProducer
{

    var client = HttpClient()


    @Producer(value = MatchList::class, endpoint = APIEndpoint.MATCHLIST)
    fun matchlist(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        var url = "https://$platform.api.riotgames.com/lol/match/v3/matchlists/by-account/$id"

        data.filterKeys { it != "platform" && it != "id" }.filter { it.component2() != null }.asIterable().forEachIndexed { i, (k, v) ->

            val value: Any? = (v as? Set<*>)?.joinToString(separator = ",") ?: v

            url += if (i == 0)
            {
                "?$k=$value"
            } else
            {
                "&$k=$value"
            }
        }


        return client.makeApiGetRequest(url)
    }

    @Producer(value = MatchIntermediary::class, endpoint = APIEndpoint.MATCH_BY_ID)
    fun match(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/match/v3/matches/$id"

        return client.makeApiGetRequest(url)
    }

    @Producer(value = MatchTimeline::class, endpoint = APIEndpoint.TIMELINE_BY_ID)
    fun timeline(data: Map<String, Any?>): HttpResponse
    {
        require(data.containsKey("platform"))
        require(data.containsKey("id"))

        val platform = data["platform"]
        val id = data["id"]
        val url = "https://$platform.api.riotgames.com/lol/match/v3/timelines/by-match/$id"

        return client.makeApiGetRequest(url)
    }
}


fun KotMaw.timeline(platform: Platform.Service, id: Long) = async {
    get<MatchTimeline>(APIEndpoint.TIMELINE_BY_ID, hashMapOf("platform" to platform, "id" to id))
}


fun KotMaw.matchlist(platform: Platform.Service, id: Long, params: () -> MatchListParams = { MatchListParams() }) = async {
    val map: HashMap<String, Any?> = hashMapOf("queue" to params().queue,
                                               "season" to params().season,
                                               "champion" to params().champion,
                                               "beginTime" to params().beginTime,
                                               "endTime" to params().endTime,
                                               "beginIndex" to params().beginIndex,
                                               "endIndex" to params().endIndex,
                                               "platform" to platform,
                                               "id" to id)

    get<MatchList>(APIEndpoint.MATCHLIST, map)
}

fun KotMaw.match(platform: Platform.Service, id: Long) = async {
    val match = get<MatchIntermediary>(APIEndpoint.MATCH_BY_ID, hashMapOf("platform" to platform, "id" to id))

    transformMatch(match)
}

fun transformMatch(match: MatchIntermediary): Match
{
    val element = JsonParser().parse(JsonUtil.toJson(match)) as JsonObject

    val participants = element.getAsJsonArray("participants")
    for (participant in participants)
    {
        val part = participant.asJsonObject

        val stats = part.getAsJsonObject("stats")
        if (!stats.has("perkPrimaryStyle"))
        {
            break
        }

        val mPerk = JsonObject()
        val array = JsonArray()

        for (i in 0..5)
        {
            val perk = JsonObject()

            perk.add("perkId", stats.get("perk" + i))
            perk.add("perkVar1", stats.get("perk" + i + "Var1"))
            perk.add("perkVar2", stats.get("perk" + i + "Var2"))
            perk.add("perkVar3", stats.get("perk" + i + "Var3"))
            array.add(perk)

            stats.remove("perk" + i)
            stats.remove("perk" + i + "Var1")
            stats.remove("perk" + i + "Var2")
            stats.remove("perk" + i + "Var3")
        }

        mPerk.add("perks", array)
        mPerk.add("perkPrimaryStyle", stats.get("perkPrimaryStyle"))
        mPerk.add("perkSubStyle", stats.get("perkSubStyle"))

        stats.remove("perkPrimaryStyle")
        stats.remove("perkSubStyle")

        part.add("perks", mPerk)
    }

    return JsonUtil.fromJson(element)
}


data class MatchTimeline(val frameInterval: Long, val frames: List<MatchFrame>)

data class MatchFrame(val timestamp: Long, val participantFrames: Map<Int, MatchParticipantFrame>, val events: List<MatchEvent>)

data class MatchParticipantFrame(val totalGold: Int,
                                 val teamScore: Int,
                                 val participantId: Int,
                                 val level: Int,
                                 val currentGold: Int,
                                 val minionsKilled: Int,
                                 val dominionScore: Int,
                                 val position: MatchPosition,
                                 val xp: Int,
                                 val jungleMinionsKilled: Int)

data class MatchPosition(val x: Int, val y: Int)

data class MatchEvent(val afterId: Int,
                      val beforeId: Int,
                      val ascendedType: String,
                      val assistingParticipantIds: List<Int>,
                      val buildingType: String,
                      val creatorId: Int,
                      val eventType: String,
                      val itemId: Int,
                      val killerId: Int,
                      val laneType: String,
                      val levelUpType: String,
                      val monsterType: String,
                      val monsterSubType: String,
                      val participantId: Int,
                      val pointCaptured: String,
                      val position: MatchPosition,
                      val skillSlot: Int,
                      val teamId: Int,
                      val timestamp: Long,
                      val towerType: String,
                      val victimId: Int,
                      val wardType: String)


data class MatchListParams(var queue: Set<Int>? = null,
                           var season: Set<Int>? = null,
                           var champion: Set<Int>? = null,
                           var beginTime: Long? = null,
                           var endTime: Long? = null,
                           var beginIndex: Int? = null,
                           var endIndex: Int? = null)

data class MatchList(val matches: List<MatchReference>, val totalGames: Int, val startIndex: Int, val endIndex: Int)

data class MatchReference(val lane: String,
                          val gameId: Long,
                          val champion: Int,
                          val platformId: String,
                          val season: Int,
                          val queue: Int,
                          val role: String,
                          val timestamp: Long)

data class MatchIntermediary(val seasonId: Int,
                             val queueId: Int,
                             val gameId: Long,
                             val participantIdentities: List<ParticipantIdentity>,
                             val gameVersion: String,
                             val platformId: String,
                             val gameMode: String,
                             val mapId: Int,
                             val gameType: String,
                             val teams: List<TeamStats>,
                             val participants: List<ParticipantIntermediary>,
                             val gameCreation: Long,
                             val gameDuration: Long)

data class Match(val seasonId: Int,
                 val queueId: Int,
                 val gameId: Long,
                 val participantIdentities: List<ParticipantIdentity>,
                 val gameVersion: String,
                 val platformId: String,
                 val gameMode: String,
                 val mapId: Int,
                 val gameType: String,
                 val teams: List<TeamStats>,
                 val participants: List<Participant>,
                 val gameCreation: Long,
                 val gameDuration: Long)

data class TeamStats(private val bans: List<BannedChampion>,
                     private val baronKills: Int,
                     private val dominionVictoryScore: Long,
                     private val dragonKills: Int,
                     private val firstBaron: Boolean,
                     private val firstBlood: Boolean,
                     private val firstDragon: Boolean,
                     private val firstInhibitor: Boolean,
                     private val firstTower: Boolean,
                     private val inhibitorKills: Int,
                     private val teamId: Int,
                     private val towerKills: Int,
                     private val vilemawKills: Int,
                     private val win: String,
                     private val firstRiftHerald: Boolean,
                     private val riftHeraldKills: Int)

data class BannedChampion(private val championId: Int, private val pickTurn: Int)

data class ParticipantIdentity(val participantId: Int, val player: Player)

data class Player(val matchHistoryUri: String,
                  val profileIcon: Int,
                  val summonerId: Long,
                  val summonerName: String,
                  val currentPlatformId: String,
                  val platformId: String,
                  val accountId: Long,
                  val currentAccountId: Long)

data class ParticipantIntermediary(val championId: Int,
                                   val highestAchievedSeasonTier: String,
                                   val participantId: Int,
                                   val runes: List<MatchRune>,
                                   val masteries: List<MatchMastery>,
                                   val perks: MatchPerks,
                                   val spell1Id: Int,
                                   val spell2Id: Int,
                                   val stats: ParticipantStatsIntermediary,
                                   val teamId: Int,
                                   val timeline: ParticipantTimeline)

data class Participant(val championId: Int,
                       val highestAchievedSeasonTier: String,
                       val participantId: Int,
                       val runes: List<MatchRune>,
                       val masteries: List<MatchMastery>,
                       val perks: MatchPerks,
                       val spell1Id: Int,
                       val spell2Id: Int,
                       val stats: ParticipantStats,
                       val teamId: Int,
                       val timeline: ParticipantTimeline)

data class MatchRune(private val runeId: Int, private val rank: Int)
data class MatchMastery(private val masteryId: Int, private val rank: Int)

data class ParticipantStatsIntermediary(val assists: Long,
                                        val champLevel: Long,
                                        val combatPlayerScore: Long,
                                        val damageSelfMitigated: Long,
                                        val damageDealtToTurrets: Long,
                                        val damageDealtToObjectives: Long,
                                        val deaths: Long,
                                        val doubleKills: Long,
                                        val firstBloodAssist: Boolean,
                                        val firstBloodKill: Boolean,
                                        val firstInhibitorAssist: Boolean,
                                        val firstInhibitorKill: Boolean,
                                        val firstTowerAssist: Boolean,
                                        val firstTowerKill: Boolean,
                                        val goldEarned: Long,
                                        val goldSpent: Long,
                                        val inhibitorKills: Long,
                                        val item0: Long,
                                        val item1: Long,
                                        val item2: Long,
                                        val item3: Long,
                                        val item4: Long,
                                        val item5: Long,
                                        val item6: Long,
                                        val killingSprees: Long,
                                        val kills: Long,
                                        val largestCriticalStrike: Long,
                                        val largestKillingSpree: Long,
                                        val largestMultiKill: Long,
                                        val longestTimeSpentLiving: Long,
                                        val magicDamageDealt: Long,
                                        val magicDamageDealtToChampions: Long,
                                        val magicalDamageTaken: Long,
                                        val neutralMinionsKilled: Long,
                                        val neutralMinionsKilledEnemyJungle: Long,
                                        val neutralMinionsKilledTeamJungle: Long,
                                        val nodeCapture: Long,
                                        val nodeCaptureAssist: Long,
                                        val nodeNeutralize: Long,
                                        val nodeNeutralizeAssist: Long,
                                        val objectivePlayerScore: Long,
                                        val pentaKills: Long,
                                        val physicalDamageDealt: Long,
                                        val physicalDamageDealtToChampions: Long,
                                        val physicalDamageTaken: Long,
                                        val quadraKills: Long,
                                        val sightWardsBoughtInGame: Long,
                                        val teamObjective: Long,
                                        val timeCCingOthers: Long,
                                        val totalDamageDealt: Long,
                                        val totalDamageDealtToChampions: Long,
                                        val totalDamageTaken: Long,
                                        val totalHeal: Long,
                                        val totalMinionsKilled: Long,
                                        val totalPlayerScore: Long,
                                        val totalScoreRank: Long,
                                        val totalTimeCrowdControlDealt: Long,
                                        val totalUnitsHealed: Long,
                                        val turretKills: Long,
                                        val tripleKills: Long,
                                        val trueDamageDealt: Long,
                                        val trueDamageDealtToChampions: Long,
                                        val trueDamageTaken: Long,
                                        val unrealKills: Long,
                                        val visionScore: Long,
                                        val visionWardsBoughtInGame: Long,
                                        val wardsKilled: Long,
                                        val wardsPlaced: Long,
                                        val win: Boolean,
                                        val playerScore0: Int,
                                        val playerScore1: Int,
                                        val playerScore2: Int,
                                        val playerScore3: Int,
                                        val playerScore4: Int,
                                        val playerScore5: Int,
                                        val playerScore6: Int,
                                        val playerScore7: Int,
                                        val playerScore8: Int,
                                        val playerScore9: Int,
                                        val perk0: Int,
                                        val perk0Var1: Int,
                                        val perk0Var2: Int,
                                        val perk0Var3: Int,
                                        val perk1: Int,
                                        val perk1Var1: Int,
                                        val perk1Var2: Int,
                                        val perk1Var3: Int,
                                        val perk2val: Int,
                                        val perk2Var1: Int,
                                        val perk2Var2: Int,
                                        val perk2Var3: Int,
                                        val perk3val: Int,
                                        val perk3Var1: Int,
                                        val perk3Var2: Int,
                                        val perk3Var3: Int,
                                        val perk4: Int,
                                        val perk4Var1: Int,
                                        val perk4Var2: Int,
                                        val perk4Var3: Int,
                                        val perk5: Int,
                                        val perk5Var1: Int,
                                        val perk5Var2: Int,
                                        val perk5Var3: Int,
                                        val perkPrimaryStyle: Int,
                                        val perkSubStyle: Int)

data class ParticipantStats(val assists: Long,
                            val champLevel: Long,
                            val combatPlayerScore: Long,
                            val damageSelfMitigated: Long,
                            val damageDealtToTurrets: Long,
                            val damageDealtToObjectives: Long,
                            val deaths: Long,
                            val doubleKills: Long,
                            val firstBloodAssist: Boolean,
                            val firstBloodKill: Boolean,
                            val firstInhibitorAssist: Boolean,
                            val firstInhibitorKill: Boolean,
                            val firstTowerAssist: Boolean,
                            val firstTowerKill: Boolean,
                            val goldEarned: Long,
                            val goldSpent: Long,
                            val inhibitorKills: Long,
                            val item0: Long,
                            val item1: Long,
                            val item2: Long,
                            val item3: Long,
                            val item4: Long,
                            val item5: Long,
                            val item6: Long,
                            val killingSprees: Long,
                            val kills: Long,
                            val largestCriticalStrike: Long,
                            val largestKillingSpree: Long,
                            val largestMultiKill: Long,
                            val longestTimeSpentLiving: Long,
                            val magicDamageDealt: Long,
                            val magicDamageDealtToChampions: Long,
                            val magicalDamageTaken: Long,
                            val neutralMinionsKilled: Long,
                            val neutralMinionsKilledEnemyJungle: Long,
                            val neutralMinionsKilledTeamJungle: Long,
                            val nodeCapture: Long,
                            val nodeCaptureAssist: Long,
                            val nodeNeutralize: Long,
                            val nodeNeutralizeAssist: Long,
                            val objectivePlayerScore: Long,
                            val pentaKills: Long,
                            val physicalDamageDealt: Long,
                            val physicalDamageDealtToChampions: Long,
                            val physicalDamageTaken: Long,
                            val quadraKills: Long,
                            val sightWardsBoughtInGame: Long,
                            val teamObjective: Long,
                            val timeCCingOthers: Long,
                            val totalDamageDealt: Long,
                            val totalDamageDealtToChampions: Long,
                            val totalDamageTaken: Long,
                            val totalHeal: Long,
                            val totalMinionsKilled: Long,
                            val totalPlayerScore: Long,
                            val totalScoreRank: Long,
                            val totalTimeCrowdControlDealt: Long,
                            val totalUnitsHealed: Long,
                            val turretKills: Long,
                            val tripleKills: Long,
                            val trueDamageDealt: Long,
                            val trueDamageDealtToChampions: Long,
                            val trueDamageTaken: Long,
                            val unrealKills: Long,
                            val visionScore: Long,
                            val visionWardsBoughtInGame: Long,
                            val wardsKilled: Long,
                            val wardsPlaced: Long,
                            val win: Boolean)

data class ParticipantTimeline(val creepsPerMinDeltas: Map<String, Double>,
                               val csDiffPerMinDeltas: Map<String, Double>,
                               val damageTakenDiffPerMinDeltas: Map<String, Double>,
                               val damageTakenPerMinDeltas: Map<String, Double>,
                               val goldPerMinDeltas: Map<String, Double>,
                               val lane: String,
                               val participantId: Long,
                               val role: String,
                               val xpDiffPerMinDeltas: Map<String, Double>,
                               val xpPerMinDeltas: Map<String, Double>)

data class MatchPerks(private val perks: List<MatchPerk>, private val perkPrimaryStyle: Int, private val perkSubStyle: Int)
data class MatchPerk(private val perkId: Int, private val perkVar1: Int, private val perkVar2: Int, private val perkVar3: Int)
