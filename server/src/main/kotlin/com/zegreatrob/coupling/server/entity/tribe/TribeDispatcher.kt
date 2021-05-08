package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher

interface TribeDispatcher : SaveTribeCommandDispatcher,
    TribeListQueryDispatcher,
    TribeQueryDispatcher
