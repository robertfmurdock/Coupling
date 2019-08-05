package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.js.Promise

external interface Coupling {
    fun spin(selectedPlayers: Array<Json>, tribeId: String): Promise<Json>
}
