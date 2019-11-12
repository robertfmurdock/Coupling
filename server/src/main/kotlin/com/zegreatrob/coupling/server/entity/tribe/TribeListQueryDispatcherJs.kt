package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher
import kotlinx.coroutines.promise

interface TribeListQueryDispatcherJs : ScopeSyntax, TribeListQueryDispatcher {
    @JsName("performTribeListQuery")
    fun performTribeListQuery() = scope.promise {
        TribeListQuery
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
