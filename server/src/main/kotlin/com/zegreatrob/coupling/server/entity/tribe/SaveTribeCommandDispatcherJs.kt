package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import kotlinx.coroutines.promise
import kotlin.js.Json

interface SaveTribeCommandDispatcherJs : ScopeSyntax, SaveTribeCommandDispatcher {
    @JsName("performSaveTribeCommand")
    fun performSaveTribeCommand(tribe: Json) = scope.promise {
        SaveTribeCommand(tribe.toTribe())
            .perform()
    }
}
