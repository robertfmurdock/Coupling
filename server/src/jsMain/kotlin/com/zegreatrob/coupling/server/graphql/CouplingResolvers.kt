package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.server.entity.boost.partyBoostResolver
import com.zegreatrob.coupling.server.entity.boost.userBoostResolver
import com.zegreatrob.coupling.server.entity.contribution.contributionResolver
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
import com.zegreatrob.coupling.server.entity.player.pairAssignmentHistoryResolve
import com.zegreatrob.coupling.server.entity.player.pairCountResolve
import com.zegreatrob.coupling.server.entity.player.pairHeatResolve
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
                if (DispatcherProviders.authorizedPartyDispatcher(r, jsonParty.id) != null) {
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
        "saveSlackIntegration" to saveSlackIntegrationResolver,
        "deleteSecret" to deleteSecretResolver,
        "deletePairAssignments" to deletePairsResolver,
        "deleteParty" to deletePartyResolver,
        "deletePin" to deletePinResolver,
        "deletePlayer" to deletePlayerResolver,
        "savePairAssignments" to savePairsResolver,
        "saveParty" to savePartyResolver,
        "savePin" to savePinResolver,
        "savePlayer" to savePlayerResolver,
        "spin" to spinResolver,
        "grantSlackAccess" to grantSlackAccessResolver,
        "grantDiscordAccess" to grantDiscordAccessResolver,
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
        "medianSpinDuration" to medianSpinDurationResolve,
        "spinsUntilFullRotation" to spinsUntilFullRotationResolve,
        "boost" to partyBoostResolver,
        "contributions" to contributionResolver,
    ),
    "Configuration" to json(
        "addToSlackUrl" to addToSlackUrlResolve,
        "discordClientId" to fun() = Config.discordClientId,
    ),
    "Pair" to json(
        "count" to pairCountResolve,
        "spinsSinceLastPaired" to spinsSinceLastPairedResolve,
        "heat" to pairHeatResolve,
        "pairAssignmentHistory" to pairAssignmentHistoryResolve,
    ),
    "User" to json(
        "boost" to userBoostResolver,
    ),
)
