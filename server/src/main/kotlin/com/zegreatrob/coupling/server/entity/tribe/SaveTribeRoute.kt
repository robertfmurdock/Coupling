package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import com.zegreatrob.coupling.server.route.dispatchCommand
import kotlin.js.Json

val saveTribeRoute = dispatchCommand { endpointHandler(::returnErrorOnFailure, ::runCommand) }

private suspend fun SaveTribeCommandDispatcher.runCommand(request: Request) = request
    .saveTribeCommand()
    .perform() to request.jsonBody()

private fun Request.saveTribeCommand() = SaveTribeCommand(jsonBody().toTribe())

private fun returnErrorOnFailure(response: Response, pair: Pair<Boolean, Json>) {
    val (successful, body) = pair
    if (successful)
        response.sendSuccessful(body)
    else
        response.sendStatus(400)
}
