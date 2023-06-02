package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.server.action.secret.SecretListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch

val secretListResolve = dispatch(
    DispatcherProviders.partyCommand,
    { _, _ -> SecretListQuery },
    ::toSerializable,
)

private fun toSerializable(result: List<PartyRecord<Secret>>) =
    result.map(PartyRecord<Secret>::toSerializable)
