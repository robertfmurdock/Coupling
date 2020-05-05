package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher

suspend fun TribeListQueryDispatcher.performTribeListQueryGQL() = TribeListQuery
    .perform()
    .map { it.toJson().add(it.data.toJson()) }
    .toTypedArray()
