package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.action.party.SavePartyCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch

val saveTribeResolver = dispatch(command, { _, args: SaveTribeInput -> SavePartyCommand(args.toModel()) }, { true })
