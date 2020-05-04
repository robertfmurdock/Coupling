package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import com.zegreatrob.coupling.server.external.express.tribeId

interface RetiredPlayersQueryDispatcherJs : RetiredPlayersQueryDispatcher, EndpointHandlerSyntax {
    val performRetiredPlayersQuery
        get() = endpointHandler(Response::sendSuccessful) {
            RetiredPlayersQuery(tribeId())
                .perform()
                .map {
                    it.toJson()
                        .add(it.data.player.toJson())
                }
                .toTypedArray()
        }
}
