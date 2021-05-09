package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val pinListResolve = dispatch(tribeCommand, { _, _ -> PinsQuery }, List<Record<TribeIdPin>>::toJsonArray)
