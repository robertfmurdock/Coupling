package com.zegreatrob.coupling.client.user

import kotlin.js.Json

external interface CouplingSocketMessage {
    val text: String
    val players: Array<Json>
}
