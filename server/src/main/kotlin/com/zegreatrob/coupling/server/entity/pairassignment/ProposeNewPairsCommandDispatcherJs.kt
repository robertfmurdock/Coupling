package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonArrayBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import kotlin.js.Json

interface ProposeNewPairsCommandDispatcherJs : ProposeNewPairsCommandDispatcher, RequestTribeIdSyntax,
    EndpointHandlerSyntax {
    @JsName("performProposeNewPairsCommand")
    val performProposeNewPairsCommand get() = endpointHandler(Response::sendSuccessful, ::handleProposeNewPairsCommand)

    private suspend fun handleProposeNewPairsCommand(request: Request) = request.proposeNewPairsCommand()
        .perform()
        .toJson()

    private fun Request.proposeNewPairsCommand() =
        ProposeNewPairsCommand(tribeId(), jsonArrayBody().map(Json::toPlayer))
}
