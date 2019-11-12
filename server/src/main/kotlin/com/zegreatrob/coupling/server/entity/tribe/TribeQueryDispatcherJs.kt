package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher
import kotlinx.coroutines.promise

interface TribeQueryDispatcherJs : TribeQueryDispatcher, ScopeSyntax {

    @JsName("performTribeQuery")
    fun performTribeQuery(tribeId: String) = scope.promise { performTribeQueryJs(tribeId) }

    private suspend fun performTribeQueryJs(tribeId: String) = TribeQuery(TribeId(tribeId))
        .perform()
        ?.toJson()
}
