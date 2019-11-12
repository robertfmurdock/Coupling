package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface RetiredPlayersQueryDispatcherJs : RetiredPlayersQueryDispatcher, ScopeSyntax {
    @JsName("performRetiredPlayersQuery")
    fun performRetiredPlayersQuery(tribeId: String) = scope.promise {
        RetiredPlayersQuery(TribeId(tribeId))
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
