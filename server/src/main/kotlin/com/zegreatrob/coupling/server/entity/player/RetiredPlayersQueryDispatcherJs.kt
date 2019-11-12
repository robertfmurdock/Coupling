package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface RetiredPlayersQueryDispatcherJs : RetiredPlayersQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    JsonSendToResponseSyntax {
    @JsName("performRetiredPlayersQuery")
    fun performRetiredPlayersQuery(request: Request, response: Response) = scope.promise {
        RetiredPlayersQuery(request.tribeId())
            .perform()
            .map { it.toJson() }
            .toTypedArray()
            .sendTo(response)
    }
}
