package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult

fun VoidResult.toJson() = when (this) {
    VoidResult.Accepted -> true
    CommandResult.Unauthorized -> null
    VoidResult.Rejected -> false
}
