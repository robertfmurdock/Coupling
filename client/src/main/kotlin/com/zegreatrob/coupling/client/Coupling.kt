package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise

external interface Coupling {
    fun saveCurrentPairAssignments(json: Json, tribeId: String): Promise<Unit>
    fun removePlayer(json: Json, value: String): Promise<Unit>
    fun deleteTribe(tribeId: String): Promise<Unit>
}

suspend fun Coupling.saveCurrentPairAssignments(pairAssignments: PairAssignmentDocument, tribeId: TribeId) {
    saveCurrentPairAssignments(pairAssignments.toJson(), tribeId.value)
            .await()
}

suspend fun Coupling.removePlayer(player: Player, tribeId: TribeId) {
    removePlayer(player.toJson(), tribeId.value)
            .await()
}

suspend fun Coupling.deleteTribe(tribeId: TribeId) {
    deleteTribe(tribeId.value)
            .await()
}
