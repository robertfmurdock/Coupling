package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.json

interface DeletePlayerCommandDispatcherJs : DeletePlayerCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    RequestPlayerIdSyntax, JsonSendToResponseSyntax {
    @JsName("performDeletePlayerCommand")
    fun performDeletePlayerCommand(request: Request, response: Response) = scope.promise {
        val result = DeletePlayerCommand(request.tribeId(), request.playerId())
            .perform()
        if (result) {
            response.sendStatus(200)
        } else {
            json("message" to "Player could not be deleted because they do not exist.")
                .sendTo(response, 404)
        }
    }

}
