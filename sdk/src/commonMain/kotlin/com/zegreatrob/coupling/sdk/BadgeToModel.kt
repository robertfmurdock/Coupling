package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.schema.type.Badge

fun Badge.toModel() = when (this) {
    Badge.Default -> com.zegreatrob.coupling.model.player.Badge.Default
    Badge.Alternate -> com.zegreatrob.coupling.model.player.Badge.Alternate
    Badge.UNKNOWN__ -> com.zegreatrob.coupling.model.player.Badge.Default
}
