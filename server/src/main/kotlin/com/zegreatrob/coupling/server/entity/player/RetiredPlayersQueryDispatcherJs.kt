package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful

interface RetiredPlayersQueryDispatcherJs : RetiredPlayersQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    @JsName("performRetiredPlayersQuery")
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
