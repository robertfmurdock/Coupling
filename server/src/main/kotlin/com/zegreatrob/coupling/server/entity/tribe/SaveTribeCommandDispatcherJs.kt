package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import kotlin.js.Json

interface SaveTribeCommandDispatcherJs : SaveTribeCommandDispatcher, EndpointHandlerSyntax {
    val performSaveTribeCommand
        get() = endpointHandler({ (successful, body) -> returnErrorOnFailure(successful, body) }) {
            SaveTribeCommand(jsonBody().toTribe())
                .perform() to jsonBody()
        }

    companion object {
        private fun Response.returnErrorOnFailure(successful: Boolean, body: Json) {
            if (successful)
                sendSuccessful(body)
            else
                sendStatus(400)
        }
    }
}
