package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher

interface TribeListQueryDispatcherJs : TribeListQueryDispatcher, EndpointHandlerSyntax {

    suspend fun performTribeListQueryGQL() = TribeListQuery
        .perform()
        .map { it.toJson() }
        .toTypedArray()
}
