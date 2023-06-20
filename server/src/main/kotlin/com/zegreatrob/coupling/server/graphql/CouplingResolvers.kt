package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.server.entity.pairassignment.currentPairAssignmentResolve
import com.zegreatrob.coupling.server.entity.pairassignment.deletePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pairassignment.savePairsResolver
import com.zegreatrob.coupling.server.entity.pairassignment.spinResolver
import com.zegreatrob.coupling.server.entity.party.deletePartyResolver
import com.zegreatrob.coupling.server.entity.party.partyListResolve
import com.zegreatrob.coupling.server.entity.party.partyResolve
import com.zegreatrob.coupling.server.entity.party.savePartyResolver
import com.zegreatrob.coupling.server.entity.pin.deletePinResolver
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.pin.savePinResolver
import com.zegreatrob.coupling.server.entity.player.deletePlayerResolver
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.player.retiredPlayerListResolve
import com.zegreatrob.coupling.server.entity.player.savePlayerResolver
import com.zegreatrob.coupling.server.entity.secret.createSecretResolver
import com.zegreatrob.coupling.server.entity.secret.deleteSecretResolver
import com.zegreatrob.coupling.server.entity.secret.secretListResolve
import com.zegreatrob.coupling.server.entity.slackaccess.grantSlackAccessResolver
import com.zegreatrob.coupling.server.entity.user.userResolve
import com.zegreatrob.coupling.server.external.express.Request
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
        "party" to { _: Json, args: Json, r: Request, _: Json ->
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
    ),
    "Mutation" to json(
        "createSecret" to createSecretResolver,
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
    ),
    "Party" to json(
        "details" to partyResolve,
        "pinList" to pinListResolve,
        "playerList" to playerListResolve,
        "retiredPlayers" to retiredPlayerListResolve,
        "pairAssignmentDocumentList" to pairAssignmentListResolve,
        "secretList" to secretListResolve,
        "currentPairAssignmentDocument" to currentPairAssignmentResolve,
    ),
)
