package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import kotlinx.coroutines.promise

interface SavePlayerCommandDispatcherJs : SavePlayerCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    JsonSendToResponseSyntax {

    @JsName("performSavePlayerCommand")
    fun performSavePlayerCommand(request: Request, response: Response) = scope.promise {
        SavePlayerCommand(
            TribeIdPlayer(request.tribeId(), request.jsonBody().toPlayer())
        )
            .perform()
            .toJson()
            .sendTo(response)
    }

}
