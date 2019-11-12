package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonArrayBody
import kotlinx.coroutines.promise
import kotlin.js.Json

interface ProposeNewPairsCommandDispatcherJs : ProposeNewPairsCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    JsonSendToResponseSyntax {
    @JsName("performProposeNewPairsCommand")
    fun performProposeNewPairsCommand(request: Request, response: Response) = scope.promise {
        ProposeNewPairsCommand(request.tribeId(), request.jsonArrayBody().map(Json::toPlayer))
            .perform()
            .toJson()
            .sendTo(response)
    }
}
