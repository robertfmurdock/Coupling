package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.sdk.schema.type.Badge

fun Badge.toDomain() = when (this) {
    Badge.Default -> com.zegreatrob.coupling.model.player.Badge.Default
    Badge.Alternate -> com.zegreatrob.coupling.model.player.Badge.Alternate
    Badge.UNKNOWN__ -> com.zegreatrob.coupling.model.player.Badge.Default
}
