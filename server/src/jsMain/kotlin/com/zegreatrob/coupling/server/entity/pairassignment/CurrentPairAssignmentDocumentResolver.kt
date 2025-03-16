package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val currentPairAssignmentResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> CurrentPairAssignmentDocumentQuery(data.id) },
    fireFunc = ::perform,

) { it?.toSerializable() }
