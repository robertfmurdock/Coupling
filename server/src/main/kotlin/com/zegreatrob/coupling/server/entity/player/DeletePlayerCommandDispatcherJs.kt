package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface DeletePlayerCommandDispatcherJs : DeletePlayerCommandDispatcher, ScopeSyntax {
    @JsName("performDeletePlayerCommand")
    fun performDeletePlayerCommand(playerId: String) = scope.promise {
        DeletePlayerCommand(TribeId(""), playerId)
            .perform()
    }
}
