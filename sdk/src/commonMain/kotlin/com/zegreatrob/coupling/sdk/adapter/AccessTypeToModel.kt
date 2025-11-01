package com.zegreatrob.coupling.sdk.adapter

import com.zegreatrob.coupling.sdk.schema.type.AccessType

fun AccessType.toModel() = when (this) {
    AccessType.Owner -> com.zegreatrob.coupling.model.AccessType.Owner
    AccessType.Player -> com.zegreatrob.coupling.model.AccessType.Player
    AccessType.UNKNOWN__ -> null
}
