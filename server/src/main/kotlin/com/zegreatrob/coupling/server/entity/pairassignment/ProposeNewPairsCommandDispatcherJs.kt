package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*
import kotlin.js.Json

interface ProposeNewPairsCommandDispatcherJs : ProposeNewPairsCommandDispatcher, EndpointHandlerSyntax {
    val performProposeNewPairsCommand get() = endpointHandler(Response::sendSuccessful, ::handleProposeNewPairsCommand)

    private suspend fun handleProposeNewPairsCommand(request: Request) = request.proposeNewPairsCommand()
        .perform()
        .toJson()

    private fun Request.proposeNewPairsCommand() =
        ProposeNewPairsCommand(
            tribeId(),
            jsonBody()["players"].unsafeCast<Array<Json>>().map(Json::toPlayer),
            jsonBody()["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
        )
}
