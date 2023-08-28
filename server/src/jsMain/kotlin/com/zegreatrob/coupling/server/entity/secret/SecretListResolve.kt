package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.server.action.secret.SecretListQuery
import com.zegreatrob.coupling.server.action.secret.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch

val secretListResolve = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _ -> data.id?.let(::PartyId)?.let { SecretListQuery(it) } },
    fireFunc = ::perform,
    toSerializable = ::toSerializable,
)

private fun toSerializable(result: List<PartyRecord<Secret>>?) = result?.map(PartyRecord<Secret>::toSerializable)
