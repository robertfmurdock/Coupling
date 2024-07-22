package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonContributionStatistics
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.entity.boost.partyBoostResolver
import com.zegreatrob.coupling.server.entity.boost.userBoostResolver
import com.zegreatrob.coupling.server.entity.contribution.clearContributionsResolver
import com.zegreatrob.coupling.server.entity.contribution.contributionResolver
import com.zegreatrob.coupling.server.entity.contribution.contributionStatisticsCountResolver
import com.zegreatrob.coupling.server.entity.contribution.contributionStatisticsMedianCycleTimeResolver
import com.zegreatrob.coupling.server.entity.contribution.contributionStatisticsWithCycleTimeCountResolver
import com.zegreatrob.coupling.server.entity.contribution.contributorResolver
import com.zegreatrob.coupling.server.entity.contribution.pairContributionResolver
import com.zegreatrob.coupling.server.entity.contribution.saveContributionResolver
import com.zegreatrob.coupling.server.entity.discord.grantDiscordAccessResolver
import com.zegreatrob.coupling.server.entity.pairassignment.currentPairAssignmentResolve
import com.zegreatrob.coupling.server.entity.pairassignment.deletePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.medianSpinDurationResolve
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.savePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.party.deletePartyResolver
import com.zegreatrob.coupling.server.entity.party.partyDetailsResolve
import com.zegreatrob.coupling.server.entity.party.partyIntegrationResolve
import com.zegreatrob.coupling.server.entity.party.partyListResolve
import com.zegreatrob.coupling.server.entity.party.savePartyResolver
import com.zegreatrob.coupling.server.entity.party.saveSlackIntegrationResolver
import com.zegreatrob.coupling.server.entity.pin.deletePinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.pin.savePinResolver
import com.zegreatrob.coupling.server.entity.player.deletePlayerResolver
import com.zegreatrob.coupling.server.entity.player.pairAssignmentHeatResolve
import com.zegreatrob.coupling.server.entity.player.pairAssignmentHistoryResolve
import com.zegreatrob.coupling.server.entity.player.pairCountResolve
import com.zegreatrob.coupling.server.entity.player.pairHeatResolve
import com.zegreatrob.coupling.server.entity.player.pairResolve
import com.zegreatrob.coupling.server.entity.player.pairsResolve
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerResolver
import com.zegreatrob.coupling.server.entity.player.spinsSinceLastPairedResolve
import com.zegreatrob.coupling.server.entity.player.spinsUntilFullRotationResolve
import com.zegreatrob.coupling.server.entity.secret.createSecretResolver
import com.zegreatrob.coupling.server.entity.secret.deleteSecretResolver
import com.zegreatrob.coupling.server.entity.secret.secretListResolve
import com.zegreatrob.coupling.server.entity.slackaccess.grantSlackAccessResolver
import com.zegreatrob.coupling.server.entity.user.userResolve
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.CouplingContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json
import kotlin.js.json

fun couplingResolvers() = json(
    "Query" to json(
        "user" to userResolve,
        "partyList" to partyListResolve,
        "party" to { _: Json, args: Json, r: CouplingContext, _: Json ->
            MainScope().promise {
                val jsonParty = kotlinx.serialization.json.Json.decodeFromDynamic<PartyInput>(args["input"])
                    .let { JsonParty(id = it.partyId) }
                val partyId = jsonParty.id
                    ?: return@promise null
                if (DispatcherProviders.authorizedPartyDispatcher(r, partyId) != null) {
                    jsonParty.let { kotlinx.serialization.json.Json.encodeToDynamic(it) }
                } else {
                    null
                }
            }
        },
        "globalStats" to globalStatsResolve,
        "config" to fun() = json(),
    ),
    "Mutation" to json(
        "createSecret" to createSecretResolver,
        "deletePairAssignments" to deletePairsResolver,
        "deleteParty" to deletePartyResolver,
        "deletePin" to deletePinResolver,
        "deletePlayer" to deletePlayerResolver,
        "deleteSecret" to deleteSecretResolver,
        "grantDiscordAccess" to grantDiscordAccessResolver,
        "grantSlackAccess" to grantSlackAccessResolver,
        "saveContribution" to saveContributionResolver,
        "clearContributions" to clearContributionsResolver,
        "savePairAssignments" to savePairsResolver,
        "saveParty" to savePartyResolver,
        "savePin" to savePinResolver,
        "savePlayer" to savePlayerResolver,
        "saveSlackIntegration" to saveSlackIntegrationResolver,
        "spin" to spinResolver,
    ),
    "Party" to json(
        "details" to partyDetailsResolve,
        "integration" to partyIntegrationResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairAssignmentDocumentList" to pairAssignmentListResolve,
        "secretList" to secretListResolve,
        "currentPairAssignmentDocument" to currentPairAssignmentResolve,
        "pairs" to pairsResolve,
        "pair" to pairResolve,
        "medianSpinDuration" to medianSpinDurationResolve,
        "spinsUntilFullRotation" to spinsUntilFullRotationResolve,
        "boost" to partyBoostResolver,
        "contributions" to contributionResolver,
        "contributors" to contributorResolver,
        "contributionStatistics" to resolver { entity: JsonParty, _: JsonNull? ->
            JsonContributionStatistics(partyId = entity.id)
        },
    ),
    "ContributionStatistics" to json(
        "count" to contributionStatisticsCountResolver,
        "medianCycleTime" to contributionStatisticsMedianCycleTimeResolver,
        "withCycleTimeCount" to contributionStatisticsWithCycleTimeCountResolver,
    ),
    "Configuration" to json(
        "addToSlackUrl" to addToSlackUrlResolve,
        "discordClientId" to fun() = Config.discordClientId,
    ),
    "Pair" to json(
        "count" to pairCountResolve,
        "spinsSinceLastPaired" to spinsSinceLastPairedResolve,
        "recentTimesPaired" to pairHeatResolve,
        "pairAssignmentHistory" to pairAssignmentHistoryResolve,
        "contributions" to pairContributionResolver,
    ),
    "PairAssignment" to json(
        "recentTimesPaired" to pairAssignmentHeatResolve,
    ),
    "User" to json(
        "boost" to userBoostResolver,
    ),
)

private inline fun <reified E, reified A, reified R> resolver(crossinline block: (E, A?) -> R) =
    { entityJson: Json?, args: Json ->
        val (entity, second) = parseGraphJsons<E, A>(entityJson, args)
        couplingJsonFormat.encodeToDynamic(block(entity, second))
    }
