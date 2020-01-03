package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax

interface PinsQueryDispatcherJs : PinsQueryDispatcher, ScopeSyntax, UserIsAuthorizedActionDispatcher {

    suspend fun performPinListQueryGQL() = PinsQuery
        .perform()
        ?.toJsonArray()

}
