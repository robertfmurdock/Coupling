package com.zegreatrob.coupling.server.action

import com.zegreatrob.testmints.action.ActionCannon

interface CannonProvider<D> {
    val cannon: ActionCannon<D>
}
