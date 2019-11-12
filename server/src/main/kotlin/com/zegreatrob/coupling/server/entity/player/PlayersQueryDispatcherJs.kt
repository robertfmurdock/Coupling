package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    JsonSendToResponseSyntax {
    @JsName("performPlayersQuery")
    fun performPlayersQuery(request: Request, response: Response) = scope.promise {
        PlayersQuery(request.tribeId())
            .perform()
            .map { it.toJson() }
            .toTypedArray()
            .sendTo(response)
    }
}
