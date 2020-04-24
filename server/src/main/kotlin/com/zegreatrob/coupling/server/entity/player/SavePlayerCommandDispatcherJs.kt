package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful

interface SavePlayerCommandDispatcherJs : SavePlayerCommandDispatcher, RequestTribeIdSyntax,
    EndpointHandlerSyntax {

    val performSavePlayerCommand
        get() = endpointHandler(Response::sendSuccessful, ::handleSavePlayerCommand)

    private suspend fun handleSavePlayerCommand(request: Request) = request.savePlayerCommand()
        .perform()
        .toJson()

    private fun Request.savePlayerCommand() = SavePlayerCommand(
        tribeId().with(jsonBody().toPlayer())
    )

}
