package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendAsResponseSyntax
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface TribeListQueryDispatcherJs : ScopeSyntax, TribeListQueryDispatcher, JsonSendAsResponseSyntax {
    @JsName("performTribeListQuery")
    fun performTribeListQuery(response: Response) = scope.promise {
        TribeListQuery
            .perform()
            .map { it.toJson() }
            .toTypedArray()
            .sendTo(response)
    }
}
