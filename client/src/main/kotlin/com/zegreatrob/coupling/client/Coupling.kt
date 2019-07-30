package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPins
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.coupling.common.toTribe
import kotlin.js.Json
import kotlin.js.Promise

external interface Coupling {
    fun getTribe(tribeId: String): Promise<Json>
    fun getTribes(): Promise<Array<Json>>
    fun getPins(tribeId: String): Promise<Array<Json>>
    fun getRetiredPlayers(tribeId: String): Promise<Array<Json>>
}

fun Coupling.getTribeAsync(tribeId: TribeId) = getTribe(tribeId.value).then { it.toTribe() }
fun Coupling.getTribeListAsync() = getTribes().then { it.map { json -> json.toTribe() } }
fun Coupling.getRetiredPlayersAsync(tribeId: TribeId) = getRetiredPlayers(tribeId.value)
        .then { jsonArray -> jsonArray.map { it.toPlayer() } }

fun Coupling.getPinListAsync(tribeId: TribeId) = getPins(tribeId.value)
        .then { jsonArray -> jsonArray.toPins() }
