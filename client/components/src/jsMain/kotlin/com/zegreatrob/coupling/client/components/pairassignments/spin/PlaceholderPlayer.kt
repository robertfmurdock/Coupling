package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotools.types.text.toNotBlankString

val placeholderPlayer = defaultPlayer.copy(
    id = PlayerId("?".toNotBlankString().getOrThrow()),
    name = "Next...",
    callSignAdjective = "--------",
    callSignNoun = "--------",
)
