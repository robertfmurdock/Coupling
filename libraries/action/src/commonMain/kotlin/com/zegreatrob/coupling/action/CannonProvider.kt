package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.ActionCannon

interface CannonProvider<out D> {
    val cannon: ActionCannon<D>
}
