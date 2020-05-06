package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.express.route.dispatchCommand
import kotlin.js.Json

val spinRoute = dispatchCommand(Request::command, { it.perform() }, PairAssignmentDocument::toJson)

private fun Request.command() = ProposeNewPairsCommand(
    tribeId(),
    jsonBody()["players"].unsafeCast<Array<Json>>().map(Json::toPlayer),
    jsonBody()["pins"].unsafeCast<Array<Json>>().map(Json::toPin)
)
