package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher

interface PinsQueryDispatcherJs : PinsQueryDispatcher {

    suspend fun performPinListQueryGQL() = PinsQuery
        .perform()
        .toJsonArray()

}
