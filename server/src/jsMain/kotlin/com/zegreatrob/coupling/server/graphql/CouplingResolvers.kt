package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.GqlAccessType
import com.zegreatrob.coupling.json.GqlPartyInput
import com.zegreatrob.coupling.json.PartyIdString
import com.zegreatrob.coupling.model.party.PairingRule.Companion.toValue
import com.zegreatrob.coupling.server.entity.boost.partyBoostResolver
import com.zegreatrob.coupling.server.entity.boost.userBoostResolver
import com.zegreatrob.coupling.server.entity.boost.userPlayerListResolve
import com.zegreatrob.coupling.server.entity.contribution.clearContributionsResolver
import com.zegreatrob.coupling.server.entity.contribution.pairContributionReportResolver
import com.zegreatrob.coupling.server.entity.contribution.partyContributionReportResolver
import com.zegreatrob.coupling.server.entity.contribution.partyContributorResolver
import com.zegreatrob.coupling.server.entity.contribution.saveContributionResolver
import com.zegreatrob.coupling.server.entity.discord.grantDiscordAccessResolver
import com.zegreatrob.coupling.server.entity.pairassignment.currentPairAssignmentResolve
import com.zegreatrob.coupling.server.entity.pairassignment.deletePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.medianSpinDurationResolve
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.savePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.party.deletePartyResolver
import com.zegreatrob.coupling.server.entity.party.getPartyDetailsResolve
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
import com.zegreatrob.coupling.server.entity.player.pairListResolve
import com.zegreatrob.coupling.server.entity.player.pairResolve
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerResolver
import com.zegreatrob.coupling.server.entity.player.spinsSinceLastPairedResolve
import com.zegreatrob.coupling.server.entity.player.spinsUntilFullRotationResolve
import com.zegreatrob.coupling.server.entity.secret.connectUserResolver
import com.zegreatrob.coupling.server.entity.secret.createConnectUserSecretResolver
import com.zegreatrob.coupling.server.entity.secret.createSecretResolver
import com.zegreatrob.coupling.server.entity.secret.deleteSecretResolver
import com.zegreatrob.coupling.server.entity.secret.disconnectUserResolver
import com.zegreatrob.coupling.server.entity.secret.secretListResolve
import com.zegreatrob.coupling.server.entity.slackaccess.grantSlackAccessResolver
import com.zegreatrob.coupling.server.entity.user.userResolve
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.external.graphql.GraphQLScalarType
import js.objects.unsafeJso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json
import kotlin.js.json

@Serializable
data class GqlPartyNode(
    val id: PartyIdString,
    val accessType: GqlAccessType? = null,
)

fun couplingResolvers() = json(
    "DateTimeISO" to makeStringScalarType("DateTimeISO"),
    "Duration" to makeStringScalarType("Duration"),
    "Query" to json(
        "user" to userResolve,
        "partyList" to partyListResolve,
        "party" to { _: Json, args: Json, r: CouplingContext, _: Json ->
            MainScope().promise {
                val input = kotlinx.serialization.json.Json.decodeFromDynamic<GqlPartyInput>(args["input"])
                val dispatcher = DispatcherProviders.authorizedPartyDispatcher(r, input.partyId)
                if (dispatcher != null) {
                    kotlinx.serialization.json.Json.encodeToDynamic(
                        GqlPartyNode(
                            id = input.partyId,
                            accessType = if (dispatcher.currentUser.authorizedPartyIds.contains(input.partyId)) {
                                GqlAccessType.Owner
                            } else {
                                GqlAccessType.Player
                            },
                        ),
                    )
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
        "createConnectUserSecret" to createConnectUserSecretResolver,
        "connectUser" to connectUserResolver,
        "disconnectUser" to disconnectUserResolver,
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
        "name" to getPartyDetailsResolve { it?.data?.name },
        "email" to getPartyDetailsResolve { it?.data?.email },
        "pairingRule" to getPartyDetailsResolve { it?.data?.pairingRule?.let(::toValue) },
        "badgesEnabled" to getPartyDetailsResolve { it?.data?.badgesEnabled },
        "defaultBadgeName" to getPartyDetailsResolve { it?.data?.defaultBadgeName },
        "alternateBadgeName" to getPartyDetailsResolve { it?.data?.alternateBadgeName },
        "callSignsEnabled" to getPartyDetailsResolve { it?.data?.callSignsEnabled },
        "animationsEnabled" to getPartyDetailsResolve { it?.data?.animationEnabled },
        "animationSpeed" to getPartyDetailsResolve { it?.data?.animationSpeed },
        "modifyingUserEmail" to getPartyDetailsResolve { it?.modifyingUserId },
        "timestamp" to getPartyDetailsResolve { it?.timestamp },
        "isDeleted" to getPartyDetailsResolve { it?.isDeleted },
        "integration" to partyIntegrationResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairingSetList" to pairAssignmentListResolve,
        "secretList" to secretListResolve,
        "currentPairingSet" to currentPairAssignmentResolve,
        "pairList" to pairListResolve,
        "pair" to pairResolve,
        "medianSpinDuration" to medianSpinDurationResolve,
        "spinsUntilFullRotation" to spinsUntilFullRotationResolve,
        "boost" to partyBoostResolver,
        "contributionReport" to partyContributionReportResolver,
    ),
    "ContributionReport" to json(
        "contributors" to partyContributorResolver,
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
        "contributionReport" to pairContributionReportResolver,
    ),
    "PairingSet" to json(
        "recentTimesPaired" to pairAssignmentHeatResolve,
    ),
    "User" to json(
        "boost" to userBoostResolver,
        "players" to userPlayerListResolve,
    ),
)

private fun makeStringScalarType(name: String) = GraphQLScalarType<String>(
    unsafeJso {
        this.name = name
        this.description = ""
        this.serialize = { "$it" }
        this.parseValue = { "$it" }
        this.parseLiteral = { it.value.unsafeCast<String>() }
    },
)
