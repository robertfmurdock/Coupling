package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.SavePartyInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.action.party.SavePartyCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch

val savePartyResolver = dispatch(command, { _, args: SavePartyInput -> SavePartyCommand(args.toModel()) }, { true })
