package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    PlayerList("playerList {${playerJsonKeys.joinToString(",")}}", "/tribe/playerList"),
    PinList("pinList {${pinJsonKeys.joinToString(",")}}", "/tribe/pinList")
}