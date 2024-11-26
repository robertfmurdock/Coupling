package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.MedianSpinDurationQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val pairAssignmentListResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> data.id?.let(::PartyId)?.let { PairAssignmentDocumentListQuery(it) } },
    fireFunc = ::perform,
    toSerializable = ::toSerializable,
)

private fun toSerializable(result: List<PartyRecord<PairAssignmentDocument>>?) = result?.map(PartyRecord<PairAssignmentDocument>::toSerializable)

val medianSpinDurationResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> data.id?.let(::PartyId)?.let { MedianSpinDurationQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it },
)
