package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise

interface TribeQueryDispatcherJs : TribeQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax {

    @JsName("performTribeQuery")
    fun performTribeQuery(request: Request) = scope.promise {
        TribeQuery(request.tribeId())
            .perform()
            ?.toJson()
    }


}

