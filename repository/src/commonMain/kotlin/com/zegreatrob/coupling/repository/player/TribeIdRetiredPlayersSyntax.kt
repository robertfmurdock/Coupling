package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdRetiredPlayersSyntax : TribeIdRetiredPlayerRecordsSyntax {
    suspend fun TribeId.loadRetiredPlayers() = loadRetiredPlayerRecords().map { it.data.player }
}
