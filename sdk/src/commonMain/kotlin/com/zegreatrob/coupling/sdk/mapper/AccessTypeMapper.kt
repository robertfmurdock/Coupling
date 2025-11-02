package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.sdk.schema.type.AccessType

fun AccessType.toDomain() = when (this) {
    AccessType.Owner -> com.zegreatrob.coupling.model.AccessType.Owner
    AccessType.Player -> com.zegreatrob.coupling.model.AccessType.Player
    AccessType.UNKNOWN__ -> null
}
