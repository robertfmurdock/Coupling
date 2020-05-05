package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.route.dispatchCommand
import kotlin.js.Json

val spinRoute get() = dispatchCommand { endpointHandler(Response::sendSuccessful, ::handleProposeNewPairsCommand) }

private suspend fun ProposeNewPairsCommandDispatcher.handleProposeNewPairsCommand(request: Request) = request
    .proposeNewPairsCommand()
    .perform()
    .toJson()

private fun Request.proposeNewPairsCommand() = ProposeNewPairsCommand(
    tribeId(),
    jsonBody()["players"].unsafeCast<Array<Json>>().map(Json::toPlayer),
    jsonBody()["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
)
