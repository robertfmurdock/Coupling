package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyListResolve = dispatch(
    dispatcherFunc = command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> PartyListQuery },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(records: List<Record<PartyDetails>>?) = records
    ?.map(Record<PartyDetails>::toSerializable)
    ?.map {
        GqlParty(
            id = it.id,
            details = it,
            boost = null,
            contributionReport = null,
            currentPairAssignmentDocument = null,
            integration = null,
            medianSpinDuration = null,
            pair = null,
            pairAssignmentDocumentList = null,
            pairs = null,
            pinList = null,
            playerList = null,
            retiredPlayers = null,
            secretList = null,
            spinsUntilFullRotation = null,
        )
    }
