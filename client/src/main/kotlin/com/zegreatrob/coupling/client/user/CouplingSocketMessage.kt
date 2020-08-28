package com.zegreatrob.coupling.client.user

import kotlin.js.Json

external interface CouplingSocketMessage {
    var text: String
    var players: Array<Json>
}
