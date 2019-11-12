package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface PinsQueryDispatcherJs : PinsQueryDispatcher, ScopeSyntax {
    @JsName("performPinsQuery")
    fun performPinsQuery(tribeId: String) = scope.promise {
        PinsQuery(TribeId(tribeId))
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
