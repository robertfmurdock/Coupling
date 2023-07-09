package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val currentPairAssignmentResolve =
    dispatch(partyCommand, { data, _ -> CurrentPairAssignmentDocumentQuery(PartyId(data.id)) }) { it?.toSerializable() }
