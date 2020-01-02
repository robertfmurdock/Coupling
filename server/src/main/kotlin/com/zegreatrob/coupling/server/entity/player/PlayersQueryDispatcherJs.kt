package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import kotlinx.coroutines.promise

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {

    @Suppress("unused")
    @JsName("performPlayerListQueryGQL")
    fun performPlayerListQueryGQL() = scope.promise {
        PlayersQuery
            .perform()
            .toJsonArray()
    }

}
