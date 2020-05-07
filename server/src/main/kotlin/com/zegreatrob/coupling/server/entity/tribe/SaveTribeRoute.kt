package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.commandDispatcher
import com.zegreatrob.coupling.server.external.express.jsonBody

val saveTribeRoute = dispatch(::commandDispatcher, ::saveTribeCommand, { it }, ::returnErrorOnFailure)

private fun saveTribeCommand(request: Request) = with(request) { SaveTribeCommand(jsonBody().toTribe()) }

private fun returnErrorOnFailure(response: Response, successful: Boolean) = if (successful)
    response.sendStatus(200)
else
    response.sendStatus(400)
