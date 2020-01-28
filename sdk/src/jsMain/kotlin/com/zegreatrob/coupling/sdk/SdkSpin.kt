package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

interface SdkSpin : AxiosSyntax {
    suspend fun requestSpin(
        tribeId: TribeId,
        players: List<Player>,
        pins: List<Pin>
    ) = axios.postAsync<Json>("/api/tribes/${tribeId.value}/spin", spinBody(players, pins))
        .await()
        .toPairAssignmentDocument()

    private inline fun spinBody(players: List<Player>, pins: List<Pin>) = json(
        "players" to players.map { it.toJson() }.toTypedArray(),
        "pins" to pins.map { it.toJson() }.toTypedArray()
    )
}