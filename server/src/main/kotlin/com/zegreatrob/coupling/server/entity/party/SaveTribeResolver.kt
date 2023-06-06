package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.json.SavePartyInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePartyResolver = dispatch(command(), { _: JsonNull, args: SavePartyInput -> SavePartyCommand(args.toModel()) }, { true })
