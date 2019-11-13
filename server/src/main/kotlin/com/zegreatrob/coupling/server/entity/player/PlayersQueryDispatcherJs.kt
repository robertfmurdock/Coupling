package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendQueryResults
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    @JsName("performPlayersQuery")
    val performPlayersQuery
        get() = endpointHandler(sendQueryResults("player")) {
            PlayersQuery(tribeId())
                .perform()
                .toJsonArray()
        }

}
