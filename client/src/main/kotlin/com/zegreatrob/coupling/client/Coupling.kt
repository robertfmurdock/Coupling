package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlin.js.Json
import kotlin.js.Promise

external interface Coupling {
    fun getTribe(tribeId: String): Promise<Json>
    fun getTribes(): Promise<Array<Json>>
    fun getPins(tribeId: String): Promise<Array<Json>>
    fun getPlayers(tribeId: String): Promise<Array<Json>>
    fun getRetiredPlayers(tribeId: String): Promise<Array<Json>>
    fun getHistory(tribeId: String): Promise<Array<Json>>
    fun spin(selectedPlayers: Array<Json>, tribeId: String): Promise<Json>
}

fun Coupling.spinAsync(selectedPlayers: List<Player>, tribeId: TribeId) =
        spin(selectedPlayers.map { it.toJson() }.toTypedArray(), tribeId.value)
                .then { it.toPairAssignmentDocument() }

fun Coupling.getTribeAsync(tribeId: TribeId) = getTribe(tribeId.value).then { it.toTribe() }
fun Coupling.getTribeListAsync() = getTribes().then { it.map { json -> json.toTribe() } }
fun Coupling.getRetiredPlayerListAsync(tribeId: TribeId) = getRetiredPlayers(tribeId.value)
        .then { jsonArray -> jsonArray.map { it.toPlayer() } }

fun Coupling.getPlayerListAsync(tribeId: TribeId) = getPlayers(tribeId.value)
        .then { jsonArray -> jsonArray.map { it.toPlayer() } }

fun Coupling.getHistoryAsync(tribeId: TribeId) = getHistory(tribeId.value)
        .then { jsonArray -> jsonArray.map { it.toPairAssignmentDocument() } }

fun Coupling.getPinListAsync(tribeId: TribeId) = getPins(tribeId.value)
        .then { jsonArray -> jsonArray.toPins() }
