package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.entity.dispatchTribeCommand

val pinListQueryRoute = dispatchTribeCommand({ PinsQuery }, { it.perform() }, List<Record<TribeIdPin>>::toJsonArray)