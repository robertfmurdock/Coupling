package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val currentPairAssignmentResolve =
    dispatch(tribeCommand, { _, _: JsonElement -> CurrentPairAssignmentDocumentQuery }) { it.toSerializable() }
