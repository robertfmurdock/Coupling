package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher

suspend fun TribeQueryDispatcher.performTribeQueryGQL(id: String) = TribeQuery(TribeId(id))
    .perform()
    ?.let {
        it.toJson().add(it.data.toJson())
    }

