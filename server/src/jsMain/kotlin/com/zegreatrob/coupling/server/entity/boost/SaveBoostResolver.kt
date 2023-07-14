package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.boost.fire
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val saveBoostResolver = dispatchAction(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonNull, args: SaveBoostInput -> SaveBoostCommand(args.partyIds) },
    fireFunc = ::fire,
    toSerializable = { true },
)
