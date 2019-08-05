package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import com.zegreatrob.coupling.common.toPins
import kotlin.js.Json
import kotlin.js.Promise

external interface Coupling {
    fun getPins(tribeId: String): Promise<Array<Json>>
    fun spin(selectedPlayers: Array<Json>, tribeId: String): Promise<Json>
}

fun Coupling.spinAsync(selectedPlayers: List<Player>, tribeId: TribeId) =
        spin(selectedPlayers.map { it.toJson() }.toTypedArray(), tribeId.value)
                .then { it.toPairAssignmentDocument() }

fun Coupling.getPinListAsync(tribeId: TribeId) = getPins(tribeId.value)
        .then { jsonArray -> jsonArray.toPins() }
